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
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;

import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloackService {

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.grant-type}")
    private String grantType;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.user-realm-url}")
    private String userRealmUrl;

    @Value("${keycloak.participant-realm-url}")
    private String participantRealmUrl;
    @Value("${registry.basePath}")
    private String registryUrl;
    @Autowired
    private ParticipantService participantService;


    public AccessTokenResponse generateToken(MultiValueMap<String, String> requestBody, String realm) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("grant_type", grantType);
        map.add("username", requestBody.getFirst("username"));
        map.add("password", requestBody.getFirst("password"));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForEntity(realm, request, AccessTokenResponse.class).getBody();
    }

    public Map<String, Object> getResponse(AccessTokenResponse response) {
        Map<String, Object> keycloakMap = new HashMap<>();
        keycloakMap.put("access_token", response.getToken());
        keycloakMap.put("expires_in", response.getExpiresIn());
        keycloakMap.put("refresh_expires_in", response.getRefreshExpiresIn());
        keycloakMap.put("refresh_token", response.getRefreshToken());
        keycloakMap.put("token_type", response.getTokenType());
        keycloakMap.put("not-before-policy", response.getNotBeforePolicy());
        keycloakMap.put("session_state", response.getSessionState());
        keycloakMap.put("scope", response.getScope());
        return keycloakMap;
    }

    public String modifyToken(String originalToken, String email) {
        try (FileInputStream keyFile = new FileInputStream("/mnt/c/Users/ASUS/Documents/keys/user-realm/private.der")) {
            String participantCode = getParticipantCode(email);
            byte[] privateKeyBytes = new byte[keyFile.available()];
            keyFile.read(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
            JWSSigner signer = new RSASSASigner(privateKey);
            SignedJWT parsedToken = SignedJWT.parse(originalToken);
            JWTClaimsSet originalPayload = parsedToken.getJWTClaimsSet();
            JWTClaimsSet.Builder modifiedPayloadBuilder = new JWTClaimsSet.Builder(originalPayload);
            modifiedPayloadBuilder.claim("participant_code", participantCode);
            JWTClaimsSet modifiedPayload = modifiedPayloadBuilder.build();
            SignedJWT newToken = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), modifiedPayload);
            newToken.sign(signer);
            return newToken.serialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getParticipantCode(String emailID) throws Exception {
        ResponseEntity<Object> searchResponse = getSuccessResponse(participantService.search(JSONUtils.deserialize(getRequestBody(emailID), Map.class), registryUrl));
        ParticipantResponse searchResp = (ParticipantResponse) searchResponse.getBody();
        Map<String, Object> userDetails = (Map<String, Object>) searchResp.getParticipants().get(0);
        return (String) userDetails.get(Constants.PARTICIPANT_CODE);
    }

    public String getRequestBody(String primaryEmail) {
        return "{ \"filters\": { \"primary_email\": { \"eq\": \" " + primaryEmail + "\" } } }";
    }

    public ResponseEntity<Object> getSuccessResponse(Object response) {
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
