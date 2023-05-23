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
import org.swasth.hcx.service.ParticipantService;
import org.swasth.hcx.service.UserService;

import java.util.Map;

import static org.swasth.common.response.ResponseMessage.INVALID_USER_ID;
import static org.swasth.common.response.ResponseMessage.PARTICIPANT_CODE_MSG;
import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class UserController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${registry.basePath}")
    private String registryUrl;
    @Value("${registry.userPath}")
    private String registryUserPath;
    @Autowired
    private UserService userService;
    @Autowired
    private ParticipantService participantService;

    @PostMapping(USER_CREATE)
    public ResponseEntity<Object> create(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            logger.info("Creating user: {}", requestBody);
            String userId = userService.createUserId(requestBody);
            requestBody.put(USER_ID,userId);
            return getSuccessResponse(participantService.invite(requestBody, registryUrl, header, userId,registryUserPath,false));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(USER_SEARCH)
    public ResponseEntity<Object> search(@RequestBody Map<String, Object> requestBody){
        try {
            logger.info("Searching participant: {}", requestBody);
            return getSuccessResponse(participantService.search(requestBody,registryUrl,registryUserPath,false));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @GetMapping(USER_READ)
    public ResponseEntity<Object> read(@PathVariable("userId") String userId){
        try {
            logger.info("Reading participant :: user Id : {}", userId);
            return getSuccessResponse(participantService.getUser(userId,registryUrl));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(USER_UPDATE)
    public ResponseEntity<Object> update(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody){
        try {
            String userId = (String) requestBody.get(USER_ID);
            Map<String, Object> details = participantService.getUser(userId, registryUrl);
            return getSuccessResponse(participantService.update(requestBody,details,registryUrl,header,userId,registryUserPath,false));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(USER_DELETE)
    public ResponseEntity<Object> delete(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            logger.info("Deleting user: {}", requestBody);
            if (!requestBody.containsKey(USER_ID))
                throw new ClientException(ErrorCodes.ERR_INVALID_USER_ID, INVALID_USER_ID);
            Map<String, Object> details = participantService.getUser((String) requestBody.get(USER_ID), registryUrl);
            return getSuccessResponse(participantService.delete(details, registryUrl, header, (String) requestBody.get(USER_ID), registryUserPath, false));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
}

    public ResponseEntity<Object> getSuccessResponse(Object response) {
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
