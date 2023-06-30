package org.swasth.hcx.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.swasth.common.dto.RegistryResponse;
import org.swasth.common.dto.Token;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.JSONUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.swasth.common.utils.Constants.*;


@Service
public class JWTService extends BaseRegistryService {

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.grant-type}")
    private String grantType;

    @Value("${keycloak.user-realm-url}")
    private String userRealmUrl;

    @Value("${keycloak.participant-realm-url}")
    private String participantRealmUrl;

    @Value("${registry.user-api-path}")
    private String registryUserPath;
    
    @Autowired
    private ParticipantService participantService;

    Map<String,Object> jwtSignerExist = new HashMap<>();

    public Map<String, Object> getToken(MultiValueMap<String, String> requestBody, String filename, String realm) throws Exception {
        AccessTokenResponse accessTokenResponse = generateToken(requestBody, realm);
        if (accessTokenResponse == null || accessTokenResponse.getToken() == null) {
            throw new ClientException("Access token response or Access token is null.");
        }
        String modifiedAccessToken = modifyToken(accessTokenResponse.getToken(), requestBody.getFirst("username"), filename);
        return getResponse(accessTokenResponse, modifiedAccessToken);
    }

    private String modifyToken(String originalToken, String email, String keyFilePath) throws Exception {
        Token jwtToken = new Token(originalToken);
        JWSSigner signer = getSigner(jwtToken.getEntityType(),keyFilePath);
        SignedJWT parsedToken = SignedJWT.parse(originalToken);
        JWTClaimsSet originalPayload = parsedToken.getJWTClaimsSet();
        JWTClaimsSet.Builder modifiedPayloadBuilder = new JWTClaimsSet.Builder(originalPayload);
        if (StringUtils.equals(jwtToken.getEntityType(),USER)) {
            Map<String, Object> userDetails = getUser(email);
            Map<String, Object> realmAccess = (Map<String, Object>) modifiedPayloadBuilder.getClaims().get("realm_access");
            realmAccess.put(TENANT_ROLES, userDetails.getOrDefault(TENANT_ROLES,new ArrayList<>()));
            modifiedPayloadBuilder.claim("realm_access", realmAccess);
            modifiedPayloadBuilder.claim(USER_ID, userDetails.get(USER_ID));
        } else if (StringUtils.equals(jwtToken.getEntityType(),ORGANISATION)) {
            modifiedPayloadBuilder.claim(PARTICIPANT_CODE, getParticipantCode((String) modifiedPayloadBuilder.getClaims().get(EMAIL)));
        }
        JWTClaimsSet modifiedPayload = modifiedPayloadBuilder.build();
        SignedJWT newToken = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), modifiedPayload);
        newToken.sign(signer);
        return newToken.serialize();
    }

    private AccessTokenResponse generateToken(MultiValueMap<String, String> requestBody, String realm) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("grant_type", grantType);
        map.add(USERNAME, requestBody.getFirst(USERNAME));
        map.add(PASSWORD, requestBody.getFirst(PASSWORD));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForEntity(realm, request, AccessTokenResponse.class).getBody();
    }

    private Map<String, Object> getResponse(AccessTokenResponse response, String accessToken) {
        Map<String, Object> keycloakMap = new HashMap<>();
        keycloakMap.put("access_token", accessToken);
        keycloakMap.put("expires_in", response.getExpiresIn());
        keycloakMap.put("refresh_expires_in", response.getRefreshExpiresIn());
        keycloakMap.put("refresh_token", response.getRefreshToken());
        keycloakMap.put("token_type", response.getTokenType());
        keycloakMap.put("not-before-policy", response.getNotBeforePolicy());
        keycloakMap.put("session_state", response.getSessionState());
        keycloakMap.put("scope", response.getScope());
        return keycloakMap;
    }

    private String getParticipantCode(String emailId) throws Exception {
        ResponseEntity<Object> searchResponse = getSuccessResponse(participantService.search(JSONUtils.deserialize(getRequestBody(PRIMARY_EMAIL, emailId), Map.class)));
        RegistryResponse searchResp = (RegistryResponse) searchResponse.getBody();
        if (searchResp == null || searchResp.getParticipants() == null || searchResp.getParticipants().isEmpty()  ) {
            throw new ClientException(ErrorCodes.ERR_ACCESS_DENIED, "Invalid credentials");
        }
        Map<String, Object> userDetails = (Map<String, Object>) searchResp.getParticipants().get(0);
        return (String) userDetails.get(PARTICIPANT_CODE);
    }

    private Map<String, Object> getUser(String emailId) throws Exception {
        RegistryResponse registryResponse = search(JSONUtils.deserialize(getRequestBody(EMAIL, emailId), Map.class), registryUserPath, USER);
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

}
