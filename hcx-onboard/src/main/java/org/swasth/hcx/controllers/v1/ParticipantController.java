package org.swasth.hcx.controllers.v1;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import kong.unirest.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.*;
import org.swasth.common.utils.*;
import org.swasth.hcx.controllers.BaseController;
import java.util.*;

import static org.swasth.common.response.ResponseMessage.INVALID_PARTICIPANT_CODE;
import static org.swasth.common.utils.Constants.*;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class ParticipantController extends BaseController {

    @Value("${registry.basePath}")
    private String registryUrl;

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.accessSecret}")
    private String accessSecret;

    @Value("${aws.region}")
    private String awsRegion;

    //TODO Remove this unnecessary code post moving changes into service layer
    public ResponseEntity<Object> responseHandler(HttpResponse<String> response, String participantCode) throws Exception {
        if (response.getStatus() == HttpStatus.OK.value()) {
            if (response.getBody().isEmpty()) {
                return getSuccessResponse("");
            } else {
                if (response.getBody().startsWith("["))
                    return getSuccessResponse(new ParticipantResponse(JSONUtils.deserialize(response.getBody(), ArrayList.class)));
                else
                    return getSuccessResponse(new ParticipantResponse(participantCode));
            }
        } else if (response.getStatus() == HttpStatus.BAD_REQUEST.value()) {
            throw new ClientException(getErrorMessage(response));
        } else if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            throw new AuthorizationException(getErrorMessage(response));
        } else if (response.getStatus() == HttpStatus.NOT_FOUND.value()) {
            throw new ResourceNotFoundException(getErrorMessage(response));
        } else {
            throw new ServerException(getErrorMessage(response));
        }
    }

    private ResponseEntity<Object> getSuccessResponse(Object response) {
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getErrorMessage(HttpResponse<String> response) throws Exception {
        Map<String, Object> result = JSONUtils.deserialize(response.getBody(), HashMap.class);
        return (String) ((Map<String, Object>) result.get("params")).get("errmsg");
    }

    //Onboard APIs move them to separate module if required
    @PostMapping(PARTICIPANT_REGISTRY_UPDATE)
    public ResponseEntity<Object> participantRegistryUpdate(@RequestBody Map<String, Object> requestBody) {
        try {
            String jwtToken = (String) requestBody.get("jwt_token");
            String endPoint = (String) requestBody.get("endpoint_url");
            String encryptionCert = (String) requestBody.get("encryption_cert");
            String signingCertPath = (String) requestBody.get("signing_cert_path");

            //split into 3 parts with . delimiter
            String[] parts = jwtToken.split("\\.");
            Map<String,Object> payload = JSONUtils.decodeBase64String(parts[1],Map.class);
            String email = (String) payload.get("email");
            Map<String, Object> participantData = getParticipant("primary_email",email);
            participantData.put("endpoint_url",endPoint);
            participantData.put("encryption_cert",encryptionCert);
            participantData.put("signing_cert_path",signingCertPath);
            String osid = (String) participantData.get(OSID);
            String participantCode = (String) participantData.get("participant_code");
            //Update the participant details
            String url = registryUrl + "/api/v1/Organisation/" + osid;
            Map<String, String> headersMap = new HashMap<>();
            headersMap.put(AUTHORIZATION, "Bearer "+jwtToken);
            HttpResponse<String> response = HttpUtils.put(url, JSONUtils.serialize(requestBody), headersMap);
            auditIndexer.createDocument(createRegistryUpdateAuditEvent(endPoint,encryptionCert,signingCertPath,email));
            if (response.getStatus() == 200 || response.getStatus() == 202) {
                return getSuccessResponse(new HashMap<>(){{put("primary_email",email);put("participant_code",participantCode);}});
            }
            return responseHandler(response, null);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    private Map<String, Object> createRegistryUpdateAuditEvent(String endPoint, String encryptionCert, String signingCertPath, String email) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ACTION, PARTICIPANT_OTP);
        event.put("endpoint_url", endPoint);
        event.put("encryption_cert", encryptionCert);
        event.put("signing_cert_path", signingCertPath);
        event.put("email", email);
        event.put("status", "success");
        event.put(ETS, System.currentTimeMillis());
        return event;
    }

    @PostMapping(PARTICIPANT_OTP)
    public ResponseEntity<Object> participantOtp(@RequestBody Map<String, Object> requestBody) {
        try {
            String phone = (String) requestBody.get("primary_mobile");
            String phoneOtp = (String) requestBody.get("phoneOtp");
            String message = "HCX mobile verification code is:" +phoneOtp;
            String phoneNumber = "+91"+ phone;  // Ex: +91XXX4374XX
            AmazonSNS snsClient = AmazonSNSClient.builder().withCredentials(new AWSCredentialsProvider() {
                @Override
                public AWSCredentials getCredentials() {
                    return new BasicAWSCredentials(accessKey, accessSecret);
                }

                @Override
                public void refresh() {

                }
            }).withRegion(awsRegion).build();

            PublishResult result = snsClient.publish(new PublishRequest()
                    .withMessage(message)
                    .withPhoneNumber(phoneNumber));
            auditIndexer.createDocument(createOtpAuditEvent(phone,phoneOtp));
            return getSuccessResponse(new HashMap<>(){{put("responseId",result.getMessageId());}});
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    private Map<String, Object> createOtpAuditEvent(String phone, String phoneOtp) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put("primary_mobile", phone);
        event.put("phone_otp", phoneOtp);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ACTION, PARTICIPANT_OTP);
        event.put("status", "success");
        event.put(ETS, System.currentTimeMillis());
        return event;
    }

    @PostMapping(PARTICIPANT_VERIFY)
    public ResponseEntity<Object> participantVerify(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            String email = (String) requestBody.get("primary_email");
            String emailOtp = (String) requestBody.get("emailOtp");
            String phoneOtp = (String) requestBody.get("phoneOtp");
            String participantCode = "";
            Map<String, Object> participantData = getParticipant("primary_email",email);
            String registryOtp = (String) participantData.get("otp_data");
            if(registryOtp.equals(emailOtp) || registryOtp.equals(phoneOtp))
            participantCode = (String) participantData.get("participant_code");
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("participant_code",participantCode);
            auditIndexer.createDocument(createVerifyAuditEvent(email,emailOtp,phoneOtp,participantCode));
            return getSuccessResponse(responseMap);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    public Map<String,Object> createVerifyAuditEvent(String email,String emailOtp,String phoneOtp,String participantCode) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put("primary_email", email);
        event.put("email_otp", emailOtp);
        event.put("phone_otp", phoneOtp);
        event.put("participant_code", participantCode);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ACTION, PARTICIPANT_VERIFY);
        event.put("status", "success");
        event.put(ETS, System.currentTimeMillis());
        return event;
    }
    private Map<String, Object> getParticipant(String key, String value) throws Exception {
        ResponseEntity<Object> searchResponse = participantSearch(JSONUtils.deserialize("{ \"filters\": { \"key\": { \"eq\": \" " + value + "\" } } }", Map.class));
        ParticipantResponse participantResponse = (ParticipantResponse) Objects.requireNonNull(searchResponse.getBody());
        if (participantResponse.getParticipants().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, INVALID_PARTICIPANT_CODE);
        return (Map<String, Object>) participantResponse.getParticipants().get(0);
    }

    public ResponseEntity<Object> participantSearch(@RequestBody Map<String, Object> requestBody) {
        try {
            String url = registryUrl + "/api/v1/Organisation/search";
            HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), new HashMap<>());
            return responseHandler(response, null);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

}
