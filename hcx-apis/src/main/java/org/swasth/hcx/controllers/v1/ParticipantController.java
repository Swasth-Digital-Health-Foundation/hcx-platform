package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.SlugUtils;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.models.Participant;
import org.swasth.hcx.service.ParticipantService;

import java.security.SecureRandom;
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

    @Value("${certificates.bucketName}")
    private String bucketName;

    @Value("${postgres.onboardingOtpTable}")
    private String onboardOtpTable;
    @Autowired
    private ParticipantService participantService;

    @PostMapping(PARTICIPANT_CREATE)
    public ResponseEntity<Object> create(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            Participant participant = new Participant(requestBody);
            participantService.validateCreateParticipant(requestBody);
            String participantCode = participant.generateCode(participant.getprimaryEmail(), fieldSeparator, hcxInstanceName);
            participantService.getCertificatesUrl(requestBody, participantCode);
            participantService.validateCertificates(requestBody);
            return participantService.invite(requestBody, registryUrl, header, participantCode);
        } catch (Exception e) {
            e.printStackTrace();
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_UPDATE)
    public ResponseEntity<Object> update(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            Participant participant = new Participant(requestBody);
            String participantCode = participant.getParticipantCode();
            participantService.getCertificatesUrl(requestBody, participantCode);
            participantService.validateUpdateParticipant(requestBody);
            Map<String, Object> participantDetails = participantService.getParticipant(participantCode,registryUrl);
            return participantService.update(requestBody, participantDetails, registryUrl, header, participantCode);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_SEARCH)
    public ResponseEntity<Object> search(@RequestParam(required = false) String fields, @RequestBody Map<String, Object> requestBody) {
        try {
            return participantService.search(requestBody, registryUrl, fields);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @GetMapping(PARTICIPANT_READ)
    public ResponseEntity<Object> read(@PathVariable("participantCode") String participantCode, @RequestParam(required = false) String fields) {
        try {
            String pathParam = "";
            if (fields != null && fields.toLowerCase().contains(SPONSORS)) {
                pathParam = SPONSORS;
            }
            return participantService.read(fields, participantCode,registryUrl, pathParam);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_DELETE)
    public ResponseEntity<Object> delete(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            if (!requestBody.containsKey(PARTICIPANT_CODE))
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, PARTICIPANT_CODE_MSG);
            Participant participant = new Participant(requestBody);
            String participantCode = participant.getParticipantCode();
            Map<String, Object> participantDetails = participantService.getParticipant(participantCode,registryUrl);
            return participantService.delete(participantDetails, registryUrl, header, participantCode);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }
}

