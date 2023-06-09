package org.swasth.hcx.service;

import kong.unirest.HttpResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.repository.support.StringQueryUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.RegistryResponse;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.response.ResponseMessage;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.SlugUtils;
import org.swasth.hcx.models.Participant;

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private ParticipantService participantService;

    public RegistryResponse create(Map<String, Object> requestBody, HttpHeaders headers, String code) throws Exception {
        HttpResponse<String> response = registryInvite(requestBody, headers, registryUserPath);
        if (response.getStatus() == 200) {
            generateUserAudit(code, requestBody, USER_CREATE);
            logger.info("Created user :: user id: {}", code);
        }
        return responseHandler(response, code, USER);
    }

    public RegistryResponse search(Map<String, Object> requestBody) throws Exception {
        return registrySearch(requestBody, registryUserPath, USER);
    }

    public RegistryResponse update(Map<String, Object> requestBody, Map<String, Object> registryDetails, HttpHeaders headers, String code) throws Exception {
        HttpResponse<String> response = registryUpdate(requestBody, registryDetails, headers, registryUserPath);
        if (response.getStatus() == 200) {
            generateUserAudit(code, requestBody, USER_UPDATE);
            logger.info("Updated user :: user id: {}", code);
        }
        return responseHandler(response, code, USER);
    }

    public RegistryResponse delete(Map<String, Object> registryDetails, HttpHeaders headers, String code) throws Exception {
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

    public RegistryResponse addUser(Map<String, Object> userBody, HttpHeaders headers) throws Exception {
        HttpResponse<String> response;
        Map<String, Object> registryDetails = getUser((String) userBody.get(USER_ID));
        ArrayList<Map<String, Object>> tenantRolesList = new ArrayList<>();
        Map<String, Object> requestBody = new HashMap<>();
        if (registryDetails.containsKey(TENANT_ROLES)) {
            tenantRolesList = JSONUtils.convert(registryDetails.getOrDefault(TENANT_ROLES, new ArrayList<>()), ArrayList.class);
            for (Map<String, Object> userExist : tenantRolesList) {
                if (userExist.get(ROLE).equals(userBody.get(ROLE)) && userExist.get(PARTICIPANT_CODE).equals(userBody.get(PARTICIPANT_CODE))) {
                    throw new ClientException("User with the role : " + userBody.get(ROLE) + " and participant code : " + userBody.get(PARTICIPANT_CODE) + " is already exist for the user_id : " + userBody.get(USER_ID));
                }
            }
        }
        requestBody.put(TENANT_ROLES, tenantRolesList);
        userBody.remove(USER_ID);
        tenantRolesList.add(userBody);
        response = registryUpdate(requestBody, registryDetails, headers, registryUserPath);
        logger.info("added role for the user_id : " + registryDetails.get(USER_ID));
        return responseHandler(response, (String) registryDetails.get(USER_ID), USER);
    }

    public RegistryResponse removeUser(Map<String, Object> requestBody, HttpHeaders headers) throws Exception {
        HttpResponse<String> response;
        String userId = (String) requestBody.get(USER_ID);
        Map<String, Object> registryDetails = getUser(userId);
        ArrayList<Map<String, Object>> filteredTenantRoles = new ArrayList<>();
        if (registryDetails.containsKey(TENANT_ROLES)) {
            ArrayList<Map<String, Object>> tenantRolesList = JSONUtils.convert(registryDetails.get(TENANT_ROLES), ArrayList.class);
            if(tenantRolesList.isEmpty()) {
               throw new ClientException("user does not have any role to remove");
            }
            for (Map<String, Object> tenantRole : tenantRolesList) {
                String role = (String) tenantRole.get(ROLE);
                String participantCode = (String) tenantRole.get(PARTICIPANT_CODE);
                if (!ALLOWED_REMOVE_ROLES.contains(role) && participantCode.equals(requestBody.get(PARTICIPANT_CODE))) {
                    filteredTenantRoles.add(tenantRole);
                } else if (tenantRole.get(ROLE).equals(ADMIN) && !tenantRole.equals(requestBody.get(PARTICIPANT_CODE))) {
                    throw new ClientException("Invalid participant code : " + requestBody.get(PARTICIPANT_CODE) + " or admin role cannot be removed.");
                }
            }
        }
        Map<String, Object> request = new HashMap<>();
        request.put(TENANT_ROLES, filteredTenantRoles);
        response = registryUpdate(request, registryDetails, headers, registryUserPath);
        logger.info("removed role for the user_id : " + registryDetails.get(USER_ID));
        return responseHandler(response, userId, USER);
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
            if (requestBody.containsKey(EMAIL) && EmailValidator.getInstance().isValid((String) requestBody.get(EMAIL))) {
                return SlugUtils.makeSlug((String) requestBody.get(EMAIL), "", fieldSeparator, hcxInstanceName);
            } else if (requestBody.containsKey(MOBILE)) {
                return requestBody.get(MOBILE) + "@" + hcxInstanceName;
            }
        }
        throw new ClientException(ErrorCodes.ERR_INVALID_USER_DETAILS, INVALID_USER_DETAILS);
    }

    public void updateAllowedFields(Map<String, Object> requestBody) throws ClientException {
        List<String> requestFields = new ArrayList<>(requestBody.keySet());
        if (ALLOWED_FIELDS_FOR_UPDATE.containsAll(requestFields)) {
            requestFields.remove(USER_ID);
            throw new ClientException(ErrorCodes.ERR_INVALID_USER_DETAILS, "Fields not allowed for update: " + requestFields);
        }
    }

    public Map<String, Object> constructRequestBody(Map<String, Object> requestBody, Map<String, Object> user) {
        Map<String, Object> userRequest = new HashMap<>();
        userRequest.put(PARTICIPANT_CODE, requestBody.get(PARTICIPANT_CODE));
        userRequest.put(ROLE, user.get(ROLE));
        userRequest.put(USER_ID, user.get(USER_ID));
        return userRequest;
    }
    
    public void authorizeToken(HttpHeaders headers, String participantCode) throws Exception {
        String token = Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0);
        Map<String,Object> payload = JSONUtils.decodeBase64String(token.split("\\.")[1], Map.class);
        String entityType =  ((List<String>) payload.get("entity")).get(0);
        boolean result = false;
        if(StringUtils.equals(entityType, "User")){
            Map<String,Object> userDetails = getUser((String) payload.get("preferred_username"));
            List<Map<String,Object>> tenantRoles = (List<Map<String, Object>>) userDetails.get(TENANT_ROLES);
            for(Map<String,Object> roleMap: tenantRoles){
                if(StringUtils.equals((String) roleMap.get(PARTICIPANT_CODE), participantCode) && StringUtils.equals((String) roleMap.get(ROLE), ADMIN)) {
                    result = true;
                }
            }
        } else if (StringUtils.equals(entityType, "Organisation")) {
            Map<String,Object> details = participantService.getParticipant(participantCode);
            if(StringUtils.equals((String) payload.get("sub"), (String) ((List<String>) details.get(OS_OWNER)).get(0))) {
                result = true;
            }
        }
        if(!result){
            throw new ClientException(ErrorCodes.ERR_ACCESS_DENIED, "Participant/User does not have permission to perform this operation");
        }
    }
    
}
