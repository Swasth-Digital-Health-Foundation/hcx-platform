package org.swasth.hcx.controllers.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.swasth.hcx.helpers.ParticipantHelper;
import org.swasth.hcx.models.Participant;
import org.swasth.hcx.service.ParticipantService;

import java.util.Map;
import java.util.Objects;

import static org.swasth.common.response.ResponseMessage.PARTICIPANT_CODE_MSG;
import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class ParticipantController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ParticipantController.class);

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
    private ParticipantService service;

    @PostMapping(PARTICIPANT_CREATE)
    public ResponseEntity<Object> create(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            logger.info("Creating participant: {}", requestBody);
            Participant participant = new Participant(requestBody);
            service.validate(requestBody, true);
            String code = participant.generateCode(participant.getprimaryEmail(), fieldSeparator, hcxInstanceName);
            service.getCertificatesUrl(requestBody, code);
            service.validateCertificates(requestBody);
            return getSuccessResponse(service.create(requestBody, header, code));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_UPDATE)
    public ResponseEntity<Object> update(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            logger.info("Updating participant: {}", requestBody);
            Participant participant = new Participant(requestBody);
            String code = participant.getParticipantCode();
            service.getCertificatesUrl(requestBody, code);
            service.validate(requestBody, false);
            Map<String, Object> details = service.getParticipant(code);
            service.authorizeEntity(Objects.requireNonNull(header.get(AUTHORIZATION)).get(0).split(" ")[1], participant.getParticipantCode(), (String) details.get(PRIMARY_EMAIL));
            return getSuccessResponse(service.update(requestBody, details, header, code));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_SEARCH)
    public ResponseEntity<Object> search(@RequestBody Map<String, Object> requestBody) {
        try {
            logger.info("Searching participant: {}", requestBody);
            return getSuccessResponse(service.search(requestBody));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @GetMapping(PARTICIPANT_READ)
    public ResponseEntity<Object> read(@PathVariable("participantCode") String code) {
        try {
            logger.info("Reading participant :: participant code: {}", code);
            return getSuccessResponse(service.read(code));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_DELETE)
    public ResponseEntity<Object> delete(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            logger.info("Deleting participant: {}", requestBody);
            if (!requestBody.containsKey(PARTICIPANT_CODE))
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, PARTICIPANT_CODE_MSG);
            Participant participant = new Participant(requestBody);
            Map<String, Object> details = service.getParticipant(participant.getParticipantCode());
            return getSuccessResponse(service.delete(details, header, participant.getParticipantCode()));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    public ResponseEntity<Object> getSuccessResponse(Object response) {
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

