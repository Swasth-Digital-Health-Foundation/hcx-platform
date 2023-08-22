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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
            if(requestBody.containsKey(EMAIL)) {
                requestBody.put(EMAIL,requestBody.get(EMAIL).toString().toLowerCase());
            }
            List<Map<String,Object>> tenantRoleList = (List<Map<String, Object>>) requestBody.getOrDefault(TENANT_ROLES,"");
            for(Map<String,Object> tenant : tenantRoleList){
                userService.authorizeToken(header,(String) tenant.getOrDefault(PARTICIPANT_CODE,""));
            }
            String userId = userService.createUserId(requestBody);
            requestBody.put(USER_ID, userId);
            return getSuccessResponse(userService.create(requestBody, userId));
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(USER_SEARCH)
    public ResponseEntity<Object> search(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            return getSuccessResponse(userService.search(header,requestBody));
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

    @PostMapping(PARTICIPANT_USER_ADD)
    public ResponseEntity<Object> addUser(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) {
        return commonController(headers, requestBody, PARTICIPANT_USER_ADD);
    }

    @PostMapping(PARTICIPANT_USER_REMOVE)
    public ResponseEntity<Object> userRemove(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) {
        return commonController(headers, requestBody, PARTICIPANT_USER_REMOVE);
    }

    public ResponseEntity<Object> getSuccessResponse(Object response) {
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> commonController(HttpHeaders headers, Map<String, Object> requestBody, String actionType) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        try {
            List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
            userService.authorizeToken(headers, (String) requestBody.get(PARTICIPANT_CODE));
            List<Map<String, Object>> users = (List<Map<String, Object>>) requestBody.get(USERS);
            Map<String, List<String>> userRolesMap = userService.constructRequestBody(users);
            for (Map.Entry<String, List<String>> entry : userRolesMap.entrySet()) {
                String userId = entry.getKey();
                List<String> roles = entry.getValue();
                future = userService.processUser(userId, roles, (String) requestBody.get(PARTICIPANT_CODE), headers, actionType);
                futures.add(future);
            }
            List<Map<String, Object>> responses = new ArrayList<>();
            for (CompletableFuture<Map<String, Object>> futureResponse : futures) {
                responses.add(futureResponse.get());
            }
            Map<String, Object> resultMap = userService.createResultMap(responses);
            String overallStatus = userService.overallStatus(responses);
            resultMap.put(OVER_ALL_STATUS, overallStatus);
            return userService.getHttpStatus(overallStatus, resultMap);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.completeExceptionally(e);
            return exceptionHandler(new Response(), e);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }
}
