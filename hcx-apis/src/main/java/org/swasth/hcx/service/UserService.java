package org.swasth.hcx.service;

import kong.unirest.HttpResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.RegistryResponse;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.JSONUtils;
import java.util.*;
import static org.swasth.common.response.ResponseMessage.INVALID_USER_DETAILS;
import static org.swasth.common.response.ResponseMessage.INVALID_USER_ID;
import static org.swasth.common.utils.Constants.*;

@Service
public class UserService extends BaseRegistryService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Value("${registry.user-api-path}")
    private String registryUserPath;
    @Autowired
    private ParticipantService participantService;

    @Autowired
    protected AuditIndexer auditIndexer;

    public RegistryResponse create(Map<String, Object> requestBody, HttpHeaders headers, String code) throws Exception {
        HttpResponse<String> response = registryInvite(requestBody, headers, registryUserPath);
        if (response.getStatus() == 200) {
            generateUserAudit(code,USER_CREATE,requestBody,(String) requestBody.get(CREATED_BY));
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
            generateUserAudit(code,USER_UPDATE,requestBody,getUserFromToken(headers));
            logger.info("Updated user :: user id: {}", code);
        }
        return responseHandler(response, code, USER);
    }

    public RegistryResponse delete(Map<String, Object> registryDetails, HttpHeaders headers, String code) throws Exception {
        HttpResponse<String> response = registryDelete(registryDetails, headers, registryUserPath);
        if (response.getStatus() == 200) {
            generateUserAudit(code,USER_DELETE,registryDetails,getUserFromToken(headers));
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
        generateAddRemoveUserAudit((String) registryDetails.get(USER_ID), PARTICIPANT_USER_ADD, userBody, getUserFromToken(headers));
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
               throw new ClientException("User does not have any role to remove");
            }
            for (Map<String, Object> tenantRole : tenantRolesList) {
                String role = (String) tenantRole.get(ROLE);
                String participantCode = (String) tenantRole.get(PARTICIPANT_CODE);
                if (!requestBody.get(ROLE).equals(role) && participantCode.equals(requestBody.get(PARTICIPANT_CODE))) {
                    filteredTenantRoles.add(tenantRole);
                }
            }
        }
        Map<String, Object> request = new HashMap<>();
        request.put(TENANT_ROLES, filteredTenantRoles);
        response = registryUpdate(request, registryDetails, headers, registryUserPath);
        generateAddRemoveUserAudit(userId, PARTICIPANT_USER_REMOVE, requestBody, getUserFromToken(headers));
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

    private void generateUserAudit(String userId, String action, Map<String, Object> requestBody, String updatedBy) throws Exception {
        Map<String,Object> edata = new HashMap<>();
        edata.put(UPDATED_BY, updatedBy);
        Map<String,Object> event = eventGenerator.createAuditLog(userId, USER, getCData(action, requestBody), edata);
        eventHandler.createUserAudit(event);
    }

    public void generateAddRemoveUserAudit(String userId, String action, Map<String, Object> requestBody, String updatedBy) throws Exception {
        Map<String,Object> edata = new HashMap<>();
        edata.put(UPDATED_BY, updatedBy);
        Map<String, Object> event = eventGenerator.createAuditLog(userId, USER, getCData(action, requestBody), edata);
        eventHandler.createUserAudit(event);
    }

    public String createUserId(Map<String, Object> requestBody) throws ClientException {
        requestBody.get(EMAIL).toString().toLowerCase();
        if (requestBody.containsKey(EMAIL) || requestBody.containsKey(MOBILE)) {
            if (requestBody.containsKey(EMAIL) && EmailValidator.getInstance().isValid((String) requestBody.get(EMAIL))) {
                return ((String) requestBody.get(EMAIL));
            } else if (requestBody.containsKey(MOBILE)) {
                return (String) requestBody.get(MOBILE);
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
        if (((List<String>) ((Map<String,Object>) payload.get("realm_access")).get("roles")).contains(ADMIN_ROLE)) {
            result = true;
        } else if (StringUtils.equals(entityType, "User")){
            Map<String,Object> userDetails = getUser((String) payload.get("preferred_username"));
            List<Map<String,Object>> tenantRoles = (List<Map<String, Object>>) userDetails.get(TENANT_ROLES);
            for(Map<String,Object> roleMap: tenantRoles){
                if(StringUtils.equals((String) roleMap.get(PARTICIPANT_CODE), participantCode) && StringUtils.equals((String) roleMap.get(ROLE), ADMIN)) {
                    result = true;
                }
            }
        } else if (StringUtils.equals(entityType, "Organisation")) {
            Map<String,Object> details = participantService.getParticipant(participantCode);
            if(StringUtils.equals((String) payload.get("sub"), ((List<String>) details.get(OS_OWNER)).get(0))) {
                result = true;
            }
        } 
        if(!result){
            throw new ClientException(ErrorCodes.ERR_ACCESS_DENIED, "Participant/User does not have permissions to perform this operation");
        }
    }
    
}
