package org.swasth.hcx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import kong.unirest.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.swasth.common.dto.RegistryResponse;
import org.swasth.common.dto.Token;
import org.swasth.common.exception.*;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

import static org.swasth.common.utils.Constants.*;


@Service
public class JWTService extends BaseRegistryService {

    @Value("${keycloak.grant-type}")
    private String grantType;
    @Value("${registry.user-api-path}")
    private String registryUserPath;
    @Autowired
    private ParticipantService participantService;

    Map<String,Object> jwtSignerExist = new HashMap<>();

    public Map<String, Object> getToken(MultiValueMap<String, String> requestBody, String filename, String realm, String clientId) throws Exception {
        String token = generateToken(requestBody, realm, clientId);
        Map<String,Object> tokenMap = JSONUtils.deserialize(token, Map.class);
        String modifiedAccessToken = modifyToken((String) tokenMap.get("access_token"), requestBody.getFirst("username"), filename, requestBody);
        return getResponse(token, modifiedAccessToken);
    }

    private String modifyToken(String originalToken, String email, String keyFilePath, MultiValueMap<String, String> requestBody) throws Exception {
        Token jwtToken = new Token(originalToken);
        JWSSigner signer = getSigner(jwtToken.getEntityType(),keyFilePath);
        SignedJWT parsedToken = SignedJWT.parse(originalToken);
        JWTClaimsSet originalPayload = parsedToken.getJWTClaimsSet();
        JWTClaimsSet.Builder modifiedPayloadBuilder = new JWTClaimsSet.Builder(originalPayload);
        if (StringUtils.equals(jwtToken.getEntityType(),USER)) {
            Map<String, Object> userDetails = getUser(getRequestBody(USER_ID, email));
            Map<String, Object> realmAccess = (Map<String, Object>) modifiedPayloadBuilder.getClaims().get(REALM_ACCESS);
            realmAccess.put(TENANT_ROLES, userDetails.getOrDefault(TENANT_ROLES,new ArrayList<>()));
            modifiedPayloadBuilder.claim(REALM_ACCESS, realmAccess);
            modifiedPayloadBuilder.claim(USER_ID, userDetails.get(USER_ID));
        } else if (StringUtils.equals(jwtToken.getEntityType(),ORGANISATION)) {
            modifiedPayloadBuilder.claim(PARTICIPANT_CODE, getParticipantDetails(PRIMARY_EMAIL, (String) modifiedPayloadBuilder.getClaims().get(EMAIL)).get(PARTICIPANT_CODE));
        } else if (StringUtils.equals(jwtToken.getEntityType(),API_ACCESS)){
            Map<String,Object> participantDetails = getParticipantDetails(PARTICIPANT_CODE, requestBody.getFirst(PARTICIPANT_CODE));
            Map<String, Object> userDetails = getUser(getUserSearchRequest(requestBody.getFirst(PARTICIPANT_CODE), requestBody.getFirst(USER_ID)));
            modifiedPayloadBuilder.claim(PARTICIPANT_CODE, requestBody.getFirst(PARTICIPANT_CODE));
            modifiedPayloadBuilder.claim(USER_ID, requestBody.getFirst(USER_ID));
            Map<String, Object> realmAccess = new HashMap<>();
            realmAccess.put("participant_roles", participantDetails.get(ROLES));
            realmAccess.put("user_roles", getUserRoles(userDetails));
            modifiedPayloadBuilder.claim(REALM_ACCESS, realmAccess);
            modifiedPayloadBuilder.claim(SUB, ((List<String>) participantDetails.get(OS_OWNER)).get(0));
        }
        JWTClaimsSet modifiedPayload = modifiedPayloadBuilder.build();
        SignedJWT newToken = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), modifiedPayload);
        newToken.sign(signer);
        return newToken.serialize();
    }

    private String generateToken(MultiValueMap<String, String> requestBody, String realmUrl, String clientId) throws JsonProcessingException, ClientException {
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/x-www-form-urlencoded");
        Map<String,Object> body = new HashMap<>();
        body.put("client_id", clientId);
        body.put("grant_type", grantType);
        body.put(USERNAME, requestBody.getFirst(USERNAME));
        body.put(PASSWORD, requestBody.getFirst(PASSWORD));
        HttpResponse<String> response = HttpUtils.post(realmUrl, body, headers);
        if(response.getStatus() != 200){
            Map<String,Object> respMap = JSONUtils.deserialize(response.getBody(), Map.class);
            throw new ClientException(ErrorCodes.ERR_ACCESS_DENIED, respMap.getOrDefault("error_description", "Internal server error").toString());
        }
        return response.getBody();
    }

    private Map<String, Object> getResponse(String response, String accessToken) throws JsonProcessingException {
        Map<String,Object> responseMap = JSONUtils.deserialize(response, Map.class);
        responseMap.put("access_token", accessToken);
        return  responseMap;
    }

    private Map<String,Object> getParticipantDetails(String key, String value) throws Exception {
        ResponseEntity<Object> searchResponse = getSuccessResponse(participantService.search(JSONUtils.deserialize(getRequestBody(key, value), Map.class)));
        RegistryResponse searchResp = (RegistryResponse) searchResponse.getBody();
        if (searchResp == null || searchResp.getParticipants() == null || searchResp.getParticipants().isEmpty()  ) {
            throw new ClientException(ErrorCodes.ERR_ACCESS_DENIED, "Invalid credentials");
        }
        return (Map<String, Object>) searchResp.getParticipants().get(0);
    }

    private Map<String, Object> getUser(String requestBody) throws JsonProcessingException, ServerException, AuthorizationException, ClientException, ResourceNotFoundException {
        RegistryResponse registryResponse = search(JSONUtils.deserialize(requestBody, Map.class), registryUserPath, USER);
        if (registryResponse.getUsers().isEmpty()) {
            throw new ClientException(ErrorCodes.ERR_INVALID_USER_DETAILS, "Invalid user details");
        }
        Map<String, Object> userDetails = (Map<String, Object>) registryResponse.getUsers().get(0);
        List<Map<String, Object>> tenantRolesList = (List<Map<String, Object>>) userDetails.getOrDefault(TENANT_ROLES, new ArrayList<>());
        List<Map<String, Object>> outputList = tenantRolesList.stream().map(tenant -> {
                    Map<String, Object> output = new HashMap<>();
                    output.put(PARTICIPANT_CODE, tenant.getOrDefault(PARTICIPANT_CODE, ""));
                    output.put(ROLE, tenant.getOrDefault(ROLE, ""));
                    return output;
                }).collect(Collectors.toList());
        Map<String,Object> response = new HashMap<>();
        response.put(USER_ID, userDetails.getOrDefault(USER_ID,""));
        response.put(TENANT_ROLES,outputList);
        return response;
    }

    private String getRequestBody(String key, String value) {
        return "{ \"filters\": { \"" + key + "\": { \"eq\": \"" + value + "\" } } }";
    }

    private JWSSigner getSigner(String entitType, String keyFilePath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKeyBytes;
        JWSSigner signer;
        if (jwtSignerExist.containsKey(entitType)) {
            signer = (JWSSigner) jwtSignerExist.get(entitType);
        } else {
            try (FileInputStream keyFile = new FileInputStream(keyFilePath)) {
                privateKeyBytes = keyFile.readAllBytes();
            }
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
            signer = new RSASSASigner(privateKey);
            jwtSignerExist.put(entitType, signer);
        }
        return signer;
    }

    private String getUserSearchRequest(String participantCode, String userId) {
        return "{ \"filters\": { \"tenant_roles.participant_code\": { \"eq\": \"" + participantCode + "\" }, \"user_id\": { \"eq\": \"" + userId + "\" } } }";
    }

    private Set<String> getUserRoles(Map<String,Object> userDetails) {
        Set<String> roles = new HashSet<>();
        for(Map<String,Object> tenantRoles: (List<Map<String,Object>>) userDetails.get(TENANT_ROLES)){
            roles.add((String) tenantRoles.get(ROLE));
        }
        return roles;
    }

    public void validate(MultiValueMap<String, String> requestBody) throws ClientException {
        if (!requestBody.containsKey(USERNAME)){
            throw new ClientException("Invalid request, user_id is mandatory");
        } else if (!requestBody.containsKey(PARTICIPANT_CODE)){
            throw new ClientException("Invalid request, participant_code is mandatory");
        }
    }

}
