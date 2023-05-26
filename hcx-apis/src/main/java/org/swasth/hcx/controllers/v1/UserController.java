package org.swasth.hcx.controllers.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.service.UserService;

import java.util.Map;

import static org.swasth.common.response.ResponseMessage.INVALID_USER_ID;
import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class UserController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @PostMapping(USER_CREATE)
    public ResponseEntity<Object> create(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            String userId = userService.createUserId(requestBody);
            requestBody.put(USER_ID, userId);
            return getSuccessResponse(userService.create(requestBody, header, userId));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(USER_SEARCH)
    public ResponseEntity<Object> search(@RequestBody Map<String, Object> requestBody) {
        try {
            return getSuccessResponse(userService.search(requestBody));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @GetMapping(USER_READ)
    public ResponseEntity<Object> read(@PathVariable("userId") String userId) {
        try {
            return getSuccessResponse(userService.getUser(userId));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(USER_UPDATE)
    public ResponseEntity<Object> update(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            if (!requestBody.containsKey(USER_ID))
                throw new ClientException(ErrorCodes.ERR_INVALID_USER_ID, INVALID_USER_ID);
            userService.updateAllowedFields(requestBody);
            String userId = (String) requestBody.get(USER_ID);
            Map<String, Object> details = userService.getUser(userId);
            return getSuccessResponse(userService.update(requestBody, details, header, userId));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(USER_DELETE)
    public ResponseEntity<Object> delete(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            if (!requestBody.containsKey(USER_ID))
                throw new ClientException(ErrorCodes.ERR_INVALID_USER_ID, INVALID_USER_ID);
            Map<String, Object> details = userService.getUser((String) requestBody.get(USER_ID));
            return getSuccessResponse(userService.delete(details, header, (String) requestBody.get(USER_ID)));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(USER_UPDATE_TENANT_ROLES)
    public ResponseEntity<Object> tenantUpdate(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) {
        try {
            if (!requestBody.containsKey(USER_ID))
                throw new ClientException(ErrorCodes.ERR_INVALID_USER_ID, INVALID_USER_ID);
            String userId = (String) requestBody.get(USER_ID);
            Map<String, Object> details = userService.getUser(userId);
            return getSuccessResponse(userService.tenantUpdate(requestBody,headers,details,userId));
        } catch (Exception e) {
            return exceptionHandler(new Response(),e);
        }
    }

    public ResponseEntity<Object> getSuccessResponse(Object response) {
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
