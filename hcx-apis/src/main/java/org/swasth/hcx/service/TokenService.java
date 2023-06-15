package org.swasth.hcx.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.swasth.common.dto.RegistryResponse;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static org.swasth.common.utils.Constants.*;


@Service
public class TokenService extends BaseRegistryService {

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.grant-type}")
    private String grantType;

    @Value("${keycloak.user-realm-url}")
    private String userRealmUrl;

    @Value("${keycloak.participant-realm-url}")
    private String participantRealmUrl;
    
    @Value("${registry.basePath}")
    private String registryUrl;

    @Value("${registry.user-api-path}")
    private String registryUserPath;
    @Autowired
    private ParticipantService participantService;
    
    public String modifyToken(String originalToken, String email, String keyFilePath) throws Exception {
        FileInputStream keyFile = new FileInputStream(keyFilePath);
           byte[] privateKeyBytes = new byte[keyFile.available()];
           keyFile.read(privateKeyBytes);
           KeyFactory keyFactory = KeyFactory.getInstance("RSA");
           PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
           RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
           JWSSigner signer = new RSASSASigner(privateKey);
           SignedJWT parsedToken = SignedJWT.parse(originalToken);
           JWTClaimsSet originalPayload = parsedToken.getJWTClaimsSet();
           JWTClaimsSet.Builder modifiedPayloadBuilder = new JWTClaimsSet.Builder(originalPayload);
           if (keyFilePath.contains("user_realm.der")) {
               Map<String,Object> userDetails = getUser(email);
               ArrayList<Map<String, Object>> tenantRolesList  = JSONUtils.convert(userDetails.getOrDefault(TENANT_ROLES, new ArrayList<>()), ArrayList.class);
               Map<String,Object> realmAccess = (Map<String, Object>) modifiedPayloadBuilder.getClaims().get("realm_access");
               realmAccess.put(TENANT_ROLES, tenantRolesList);
               modifiedPayloadBuilder.claim("realm_access", realmAccess);
               modifiedPayloadBuilder.claim(USER_ID, userDetails.get(USER_ID));
           } else if (keyFilePath.contains("participant_realm.der")) {
               modifiedPayloadBuilder.claim(PARTICIPANT_CODE, getParticipantCode((String) modifiedPayloadBuilder.getClaims().get(EMAIL)));
           }
           
           JWTClaimsSet modifiedPayload = modifiedPayloadBuilder.build();
           SignedJWT newToken = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), modifiedPayload);
           newToken.sign(signer);
           return newToken.serialize();
   }

    public AccessTokenResponse generateToken(MultiValueMap<String, String> requestBody, String realm) {
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

    public Map<String, Object> getResponse(AccessTokenResponse response,String accessToken) {
        Map<String, Object> keycloakMap = new HashMap<>();
        keycloakMap.put("access_token",accessToken);
        keycloakMap.put("expires_in", response.getExpiresIn());
        keycloakMap.put("refresh_expires_in", response.getRefreshExpiresIn());
        keycloakMap.put("refresh_token", response.getRefreshToken());
        keycloakMap.put("token_type", response.getTokenType());
        keycloakMap.put("not-before-policy", response.getNotBeforePolicy());
        keycloakMap.put("session_state", response.getSessionState());
        keycloakMap.put("scope", response.getScope());
        return keycloakMap;
    }

    public Map<String,Object> getToken(MultiValueMap<String, String> requestBody, String filename) throws Exception {
        AccessTokenResponse accessTokenResponse = generateToken(requestBody,participantRealmUrl);
        String modifiedAccessToken = modifyToken(accessTokenResponse.getToken(),requestBody.getFirst("username"), filename);
        Map<String,Object> response = getResponse(accessTokenResponse,modifiedAccessToken);
        return response;
    }

    public String getParticipantCode(String emailId) throws Exception {
        ResponseEntity<Object> searchResponse = getSuccessResponse(participantService.search(JSONUtils.deserialize(getRequestBody(PRIMARY_EMAIL, emailId), Map.class)));
        RegistryResponse searchResp = (RegistryResponse) searchResponse.getBody();
        if (searchResp.getParticipants().isEmpty()) {
            throw new ClientException(ErrorCodes.ERR_ACCESS_DENIED, "Invalid credentials");
        }
        Map<String, Object> userDetails = (Map<String, Object>) searchResp.getParticipants().get(0);
        return (String) userDetails.get(PARTICIPANT_CODE);
    }

    public String getRequestBody(String key, String value) {
        return "{ \"filters\": { \"" + key + "\": { \"eq\": \"" + value + "\" } } }";
    }

    public ResponseEntity<Object> getSuccessResponse(Object response) {
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public Map<String, Object> getUser(String emailId) throws Exception {
        RegistryResponse registryResponse = registrySearch(JSONUtils.deserialize(getRequestBody(EMAIL, emailId),Map.class),registryUserPath,USER);
        Map<String,Object> userDetails = (Map<String, Object>) registryResponse.getUsers().get(0);
        return userDetails;
    }
}
