package org.swasth.hcx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kong.unirest.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.RegistryResponse;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.dto.Token;
import org.swasth.common.exception.*;
import org.swasth.common.utils.JSONUtils;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
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
    private KeycloakApiAccessService apiAccessService;
    public RegistryResponse create(Map<String, Object> requestBody, String code) throws Exception {
        HttpResponse<String> response = invite(requestBody, registryUserPath);
        if (response.getStatus() == 200) {
            generateUserAudit(code, USER_CREATE, requestBody, (String) requestBody.get(CREATED_BY));
        }
        return responseHandler(response, code, USER);
    }

    public RegistryResponse search(HttpHeaders headers, Map<String, Object> requestBody) throws ServerException, AuthorizationException, ClientException, ResourceNotFoundException, JsonProcessingException {
        return authorizeSearch(headers, requestBody);
    }

    public RegistryResponse update(Map<String, Object> requestBody, Map<String, Object> registryDetails, HttpHeaders headers, String code) throws Exception {
        HttpResponse<String> response = update(requestBody, registryDetails, registryUserPath);
        if (response.getStatus() == 200) {
            generateUserAudit(code, USER_UPDATE, requestBody, getUserFromToken(headers));
        }
        return responseHandler(response, code, USER);
    }

    public RegistryResponse delete(Map<String, Object> registryDetails, HttpHeaders headers, String code) throws Exception {
        HttpResponse<String> response = delete(registryDetails, registryUserPath);
        if (response.getStatus() == 200) {
            generateUserAudit(code, USER_DELETE, registryDetails, getUserFromToken(headers));
            RegistryResponse registryResponse = new RegistryResponse(code, USER);
            registryResponse.setStatus(INACTIVE);
            return registryResponse;
        }
        return responseHandler(response, code, USER);
    }

    @Async
    public CompletableFuture<Map<String, Object>> processUser(String userId, List<String> roles, String participantCode, HttpHeaders headers, String action) {
        Map<String, Object> registryDetails = new HashMap<>();
        Map<String, Object> responseMap = new HashMap<>();
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        try {
            HttpResponse<String> response;
            registryDetails = getUser(userId);
            Map<String, Object> finalRequest = new HashMap<>();
            ArrayList<Map<String, Object>> tenantRolesList = JSONUtils.convert(registryDetails.get(TENANT_ROLES), ArrayList.class);
            if (StringUtils.equals(action, PARTICIPANT_USER_ADD)) {
                addUser(roles, participantCode, tenantRolesList, finalRequest);
                apiAccessService.addUserWithParticipant(userId, participantCode, (String) registryDetails.get(USER_NAME));
            } else if (StringUtils.equals(action, PARTICIPANT_USER_REMOVE)) {
                ArrayList<Map<String, Object>> filteredTenantRoles = new ArrayList<>();
                removeUser(roles, participantCode, finalRequest, tenantRolesList, filteredTenantRoles);
                if (roles.contains(ADMIN)) {
                    apiAccessService.removeUserWithParticipant(participantCode, userId);
                }
            }
            response = update(finalRequest, registryDetails, registryUserPath);
            generateAddRemoveUserAudit(userId, action, tenantRolesList.get(0), getUserFromToken(headers));
            if (response.getStatus() == 200) {
                responseMap = getResponse((String) registryDetails.get(USER_ID), SUCCESSFUL);
            }
            future.complete(responseMap);
            return future;
        } catch (Exception e) {
            responseMap = getResponse((String) registryDetails.get(USER_ID), FAILED);
            responseMap.put(ERROR, new ResponseError(null, e.getMessage(), null));
            future.complete(responseMap);
            return future;
        }
    }

    @Async
    private void addUser(List<String> roles, String participantCode, List<Map<String, Object>> tenantRolesList, Map<String, Object> finalRequest) throws ClientException {
        for (String role : roles) {
            for (Map<String, Object> tenantRole : tenantRolesList) {
                if (tenantRole.get(ROLE).equals(role) && tenantRole.get(PARTICIPANT_CODE).equals(participantCode)) {
                    throw new ClientException("Role '" + role + "' with Participant Code '" + participantCode + " already exists.");
                }
            }
        }
        for (String role : roles) {
            tenantRolesList.add(constructRequest(role, participantCode));
        }
        finalRequest.put(TENANT_ROLES, tenantRolesList);
    }

    public Map<String, Object> constructRequest(String role, String participantCode) {
        Map<String, Object> request = new HashMap<>();
        request.put(PARTICIPANT_CODE, participantCode);
        request.put(ROLE, role);
        return request;
    }
    @Async
    private void removeUser(List<String> roles, String participantCode, Map<String, Object> finalRequest, ArrayList<Map<String, Object>> tenantRolesList, ArrayList<Map<String, Object>> filteredTenantRoles) throws ClientException {
        if (tenantRolesList.isEmpty()) {
            throw new ClientException("User does not have any role to remove");
        }
        for (Map<String, Object> tenantRole : tenantRolesList) {
         if (!roles.contains(tenantRole.get(ROLE).toString()) && tenantRole.get(PARTICIPANT_CODE).equals(participantCode)) {
                filteredTenantRoles.add(tenantRole);
            }
        }
        finalRequest.put(TENANT_ROLES, filteredTenantRoles);
    }

    private Map<String, Object> getResponse(String userId, String status) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(USER_ID, userId);
        responseMap.put("status", status);
        return responseMap;
    }

    public Map<String, Object> getUser(String userId) throws JsonProcessingException, ServerException, AuthorizationException, ClientException, ResourceNotFoundException {
        logger.debug("searching for :: user id : {}", userId);
        ResponseEntity<Object> searchResponse = getSuccessResponse(search(JSONUtils.deserialize(getUserRequest(userId), Map.class), registryUserPath, USER));
        RegistryResponse registryResponse = (RegistryResponse) Objects.requireNonNull(searchResponse.getBody());
        if (registryResponse.getUsers().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_USER_ID, INVALID_USER_ID);
        return registryResponse.getUsers().get(0);
    }

    private String getUserRequest(String userId) {
        return "{ \"filters\": { \"user_id\": { \"eq\": \" " + userId + "\" } } }";
    }

    private void generateUserAudit(String userId, String action, Map<String, Object> requestBody, String updatedBy) throws Exception {
        Map<String, Object> edata = new HashMap<>();
        if (StringUtils.equals(action, USER_CREATE)) {
            edata.put("createdBy", updatedBy);
        } else {
            edata.put(UPDATED_BY, updatedBy);
        }
        Map<String, Object> event = eventGenerator.createAuditLog(userId, USER, getCData(action, requestBody), edata);
        eventHandler.createUserAudit(event);
    }

    public void generateAddRemoveUserAudit(String userId, String action, Map<String, Object> requestBody, String updatedBy) throws Exception {
        Map<String, Object> edata = new HashMap<>();
        edata.put(UPDATED_BY, updatedBy);
        Map<String, Object> event = eventGenerator.createAuditLog(userId, USER, getCData(action, requestBody), edata);
        eventHandler.createUserAudit(event);
    }

    public String createUserId(Map<String, Object> requestBody) throws ClientException {
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
        for (String fields : requestFields) {
            if (NOT_ALLOWED_FIELDS_FOR_UPDATE.contains(fields)) {
                requestFields.remove(USER_ID);
                throw new ClientException(ErrorCodes.ERR_INVALID_USER_DETAILS, "Fields not allowed for update: " + requestFields);
            }
        }
    }

    public void authorizeToken(HttpHeaders headers, String participantCode) throws Exception {
        Token token = new Token(Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0));
        boolean result = false;
        if (token.getRoles().contains(ADMIN_ROLE)) {
            result = true;
        } else if (StringUtils.equals(token.getEntityType(), USER)) {
            for (Map<String, String> roleMap : token.getTenantRoles()) {
                if (StringUtils.equals(roleMap.get(PARTICIPANT_CODE), participantCode) && StringUtils.equals(roleMap.get(ROLE), ADMIN)) {
                    result = true;
                }
            }
        } else if (StringUtils.equals(token.getEntityType(), ORGANISATION)) {
            Map<String, Object> details = participantService.getParticipant(participantCode);
            if (StringUtils.equals(token.getSubject(), ((List<String>) details.get(OS_OWNER)).get(0))) {
                result = true;
            }
        }
        if (!result) {
            throw new ClientException(ErrorCodes.ERR_ACCESS_DENIED, "Participant/User does not have permissions to perform this operation");
        }
    }

    public String overallStatus(List<Map<String, Object>> responses) {
        boolean successful = false;
        boolean failed = false;
        for (Map<String, Object> response : responses) {
            String status = (String) response.get("status");
            if (status.equals(SUCCESSFUL)) {
                successful = true;
            } else if (status.equals(FAILED)) {
                failed = true;
            }
        }
        return getStatus(successful, failed);
    }

    private String getStatus(boolean successful, boolean failed) {
        String overallStatus = "";
        if (failed && successful) {
            overallStatus = PARTIAL;
        } else if (successful) {
            overallStatus = SUCCESSFUL;
        } else if (failed) {
            overallStatus = FAILED;
        }
        return overallStatus;
    }

    public Map<String, Object> createResultMap(List<Map<String, Object>> responses) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(RESULT, responses);
        resultMap.put(TIME_STAMP, System.currentTimeMillis());
        return resultMap;
    }

    public ResponseEntity<Object> getHttpStatus(String overAllStatus, Object response) {
        switch (overAllStatus) {
            case SUCCESSFUL:
                return new ResponseEntity<>(response, HttpStatus.OK);
            case FAILED:
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            case PARTIAL:
                return new ResponseEntity<>(response, HttpStatus.PARTIAL_CONTENT);
            default:
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public RegistryResponse authorizeSearch(HttpHeaders headers, Map<String, Object> requestBody) throws JsonProcessingException, ServerException, AuthorizationException, ClientException, ResourceNotFoundException {
        Token token = new Token(Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0));
        if (token.getRoles().contains(ADMIN_ROLE)) {
            return search(requestBody, registryUserPath, USER);
        } else if (StringUtils.equals(token.getEntityType(), USER) && !token.getTenantRoles().isEmpty()) {
            return search(JSONUtils.deserialize(getOrganisationUsers(token, requestBody), Map.class), registryUserPath, USER);
        } else {
            throw new ClientException(ErrorCodes.ERR_INVALID_JWT, "User is not allowed to perform this operation");
        }
    }

    public String getOrganisationUsers(Token token, Map<String, Object> requestBody) throws ClientException, JsonProcessingException {
        Set<String> organisations = token.getTenantRoles().stream()
                .map(tenantMap -> tenantMap.get(PARTICIPANT_CODE))
                .collect(Collectors.toSet());
        Map<String, Object> filters = (Map<String, Object>) requestBody.getOrDefault(FILTERS, "");
        if (filters.containsKey("tenant_roles.participant_code")) {
            throw new ClientException("This field is not allowed in filters : tenant_roles.participant_code");
        }
        filters.put("tenant_roles.participant_code", Map.of("or", organisations));
        return JSONUtils.serialize(requestBody);
    }
    public Map<String, List<String>> constructRequestBody(List<Map<String, Object>> userList) {
        Map<String, List<String>> userRolesMap = new HashMap<>();
        for (Map<String, Object> user : userList) {
            String userId = (String) user.get(USER_ID);
            String role = (String) user.get(ROLE);
            userRolesMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(role);
        }
        return userRolesMap;
    }
}

