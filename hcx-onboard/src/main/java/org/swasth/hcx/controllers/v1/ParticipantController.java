package org.swasth.hcx.controllers.v1;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import kong.unirest.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.*;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;
import org.swasth.hcx.controllers.BaseController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.swasth.common.response.ResponseMessage.INVALID_PARTICIPANT_CODE;
import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class ParticipantController extends BaseController {

    @Value("${registry.basePath}")
    private String registryUrl;

    @Value("${email.subject}")
    private String subject;

    @Value("${email.message}")
    private String message;

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

    @PostMapping(PARTICIPANT_VERIFY_OTP)
    public ResponseEntity<Object> participantVerify(@RequestBody Map<String, Object> requestBody) {
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
            auditIndexer.createDocument(eventGenerator.createVerifyAuditEvent(email,emailOtp,phoneOtp,participantCode));
            return getSuccessResponse(responseMap);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    private Map<String, Object> getParticipant(String key, String value) throws Exception {
        ResponseEntity<Object> searchResponse = participantSearch(JSONUtils.deserialize("{ \"filters\": { \""+key+"\": { \"eq\": \" " + value + "\" } } }", Map.class));
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
