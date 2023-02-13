package org.swasth.hcx.controllers.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.models.Participant;
import org.swasth.hcx.service.ParticipantService;

import java.util.Map;

import static org.swasth.common.response.ResponseMessage.PARTICIPANT_CODE_MSG;
import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class ParticipantController extends BaseController {

    @Value("${registry.basePath}")
    private String registryUrl;

    @Value("${redis.expires}")
    private int redisExpires;

    @Value("${participantCode.fieldSeparator}")
    private String fieldSeparator;

    @Value("${hcx.instanceName}")
    private String hcxInstanceName;

    @Value("${postgres.onboardingTable}")
    private String onboardingTable;

    @Value("${postgres.onboardingOtpTable}")
    private String onboardOtpTable;

    private ParticipantService service;

    @PostMapping(PARTICIPANT_CREATE)
    public ResponseEntity<Object> participantCreate(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            Participant participant = new Participant(requestBody);
            service.validate(requestBody,true);
            String code = participant.generateCode(participant.getprimaryEmail(), fieldSeparator, hcxInstanceName);
            service.getCertificatesUrl(requestBody, code);
            service.validateCertificates(requestBody);
            return getSuccessResponse(service.invite(requestBody, registryUrl, header, code));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_UPDATE)
    public ResponseEntity<Object> participantUpdate(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            Participant participant = new Participant(requestBody);
            String code = participant.getParticipantCode();
            service.getCertificatesUrl(requestBody, code);
            service.validate(requestBody,false);
            Map<String, Object> details = service.getParticipant(code,registryUrl);
            return getSuccessResponse(service.update(requestBody, details, registryUrl, header, code));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }


    @PostMapping(PARTICIPANT_SEARCH)
    public ResponseEntity<Object> participantSearch(@RequestParam(required = false) String fields, @RequestBody Map<String, Object> requestBody) throws JsonProcessingException {
        try {
            return getSuccessResponse(service.search(requestBody, registryUrl, fields));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @GetMapping(PARTICIPANT_READ)

    public ResponseEntity<Object> read(@PathVariable("participantCode") String code, @RequestParam(required = false) String fields) {
        try {
            String pathParam = "";
            if(fields != null && fields.toLowerCase().contains(SPONSORS)){
                pathParam = SPONSORS;
            }
            return service.read(fields,code,registryUrl,pathParam);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }


    @PostMapping(PARTICIPANT_DELETE)
    public ResponseEntity<Object> participantDelete(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            if (!requestBody.containsKey(PARTICIPANT_CODE))
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, PARTICIPANT_CODE_MSG);
            Participant participant = new Participant(requestBody);
            String code = participant.getParticipantCode();
            Map<String, Object> details = service.getParticipant(code,registryUrl);
            return getSuccessResponse(service.delete(details, registryUrl, header, code));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }
    public ResponseEntity<Object> getSuccessResponse(Object response) {
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

