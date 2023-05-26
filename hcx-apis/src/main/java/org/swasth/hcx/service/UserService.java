package org.swasth.hcx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kong.unirest.HttpResponse;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.RegistryResponse;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.SlugUtils;

import java.util.*;

import static org.swasth.common.response.ResponseMessage.INVALID_USER_DETAILS;
import static org.swasth.common.response.ResponseMessage.INVALID_USER_ID;
import static org.swasth.common.utils.Constants.*;

@Service
public class UserService extends BaseRegistryService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Value("${registry.user-api-path}")
    private String registryUserPath;
    @Value("${participantCode.fieldSeparator}")
    private String fieldSeparator;
    @Value("${hcx.instanceName}")
    private String hcxInstanceName;

    public RegistryResponse create(Map<String, Object> requestBody, HttpHeaders headers, String code) throws Exception {
        logger.info("Creating user: {}", requestBody);
        HttpResponse<String> response = registryInvite(requestBody, headers, registryUserPath);
        if (response.getStatus() == 200) {
            generateUserAudit(code, requestBody, USER_CREATE);
            logger.info("Created user :: user id: {}", code);
        }
        return responseHandler(response, code, USER);
    }

    public RegistryResponse search(Map<String, Object> requestBody) throws Exception {
        logger.info("Searching participant: {}", requestBody);
        return registrySearch(requestBody, registryUserPath,USER);
    }

    public RegistryResponse update(Map<String, Object> requestBody, Map<String, Object> registryDetails, HttpHeaders headers, String code) throws Exception {
        logger.info("Updating user: {}", requestBody);
        HttpResponse<String> response = registryUpdate(requestBody, registryDetails, headers, registryUserPath);
        if (response.getStatus() == 200) {
            generateUserAudit(code, requestBody, USER_UPDATE);
            logger.info("Updated user :: user id: {}", code);
        }
        return responseHandler(response, code, USER);
    }

    public RegistryResponse delete(Map<String, Object> registryDetails, HttpHeaders headers, String code) throws Exception {
        logger.info("Deleting user: {}", code);
        HttpResponse<String> response = registryDelete(registryDetails, headers, registryUserPath);
        if (response.getStatus() == 200) {
            generateUserAudit(code, registryDetails, USER_DELETE);
            logger.info("User deleted :: userId: {}", code);
            RegistryResponse registryResponse = new RegistryResponse(code, USER);
            registryResponse.setStatus(INACTIVE);
            return registryResponse;
        }
        return responseHandler(response, code, USER);
    }

    public RegistryResponse tenantUpdate(Map<String, Object> requestBody, HttpHeaders headers, Map<String, Object> registryDetails, String code) throws Exception {
        logger.info("Updating tenant roles: {}", requestBody);
        HttpResponse<String> response;
        if (registryDetails.containsKey(TENANT_ROLES)) {
            ArrayList<Map<String, Object>> tenantRolesList = JSONUtils.convert(registryDetails.getOrDefault(TENANT_ROLES, ""), ArrayList.class);
            List<Map<String, Object>> requestBodyList = (List<Map<String, Object>>) requestBody.get(TENANT_ROLES);
            tenantRolesList.add(requestBodyList.get(0));
            requestBody.put(TENANT_ROLES, tenantRolesList);
        }
        response = registryUpdate(requestBody, registryDetails, headers, registryUserPath);
        return responseHandler(response, code, USER);
    }

    public Map<String, Object> getUser(String userId) throws Exception {
        logger.info("searching for :: user id : {}", userId);
        ResponseEntity<Object> searchResponse = getSuccessResponse(search(JSONUtils.deserialize(getUserRequest(userId), Map.class)));
        RegistryResponse registryResponse = (RegistryResponse) Objects.requireNonNull(searchResponse.getBody());
        if (registryResponse.getUsers().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_USER_ID, INVALID_USER_ID);
        return (Map<String, Object>) registryResponse.getUsers().get(0);
    }

    private String getUserRequest(String userId) {
        return "{ \"filters\": { \"user_id\": { \"eq\": \" " + userId + "\" } } }";
    }

    private void generateUserAudit(String userId, Map<String, Object> requestBody, String action) throws Exception {
        eventHandler.createAudit(eventGenerator.createAuditLog(userId, USER, getCData(action, requestBody), new HashMap<>()));
    }

    public String createUserId(Map<String, Object> requestBody) throws ClientException {
        if (requestBody.containsKey(EMAIL) || requestBody.containsKey(MOBILE)) {
            if (requestBody.containsKey(EMAIL) && EmailValidator.getInstance().isValid((String) requestBody.get(EMAIL) )) {
                return SlugUtils.makeSlug((String) requestBody.get(EMAIL), "", fieldSeparator, hcxInstanceName);
            } else if (requestBody.containsKey(MOBILE)) {
                return requestBody.get(MOBILE) + "@" + hcxInstanceName;
            }
        }
        throw new ClientException(ErrorCodes.ERR_INVALID_USER_DETAILS,INVALID_USER_DETAILS);
    }

    public void updateAllowedFields(Map<String, Object> requestBody) throws ClientException {
        List<String> requestFields = new ArrayList<>(requestBody.keySet());
        if (!NOT_ALLOWED_FIELDS_FOR_UPDATE.containsAll(requestFields)) {
            requestFields.remove(USER_ID);
            throw new ClientException(ErrorCodes.ERR_UPDATE_PARTICIPANT_DETAILS, "Fields not allowed for update: " + requestFields);
        }
    }
}
