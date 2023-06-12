package org.swasth.hcx.controllers.v1;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.swasth.common.dto.Response;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.service.KeycloackService;

import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.GENERATE_TOKEN;
import static org.swasth.common.utils.Constants.VERSION_PREFIX;

@RestController()
@RequestMapping(VERSION_PREFIX)
public class KeycloakController extends BaseController {

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

    @Autowired
    private KeycloackService keycloackService;

    @PostMapping(GENERATE_TOKEN)
    public ResponseEntity<Object> generateToken(@RequestBody MultiValueMap<String, String> requestBody) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("grant_type", grantType);
        map.add("client_secret", clientSecret);
        map.add("username", requestBody.getFirst("username"));
        map.add("password", requestBody.getFirst("password"));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        AccessTokenResponse response = restTemplate.postForEntity(participantRealmUrl, request, AccessTokenResponse.class).getBody();
        String existingToken = response.getToken();
        Map<String,Object> jwt = keycl(existingToken);
        return ResponseEntity.ok(jwt);
    }

    public Map<String,Object> keycl(String originalToken) {
        try (FileInputStream keyFile = new FileInputStream("/mnt/c/Users/ASUS/Documents/keys/swasth-realm/private.der")) {
            byte[] privateKeyBytes = new byte[keyFile.available()];
            keyFile.read(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
            JWSSigner signer = new RSASSASigner(privateKey);
            SignedJWT parsedToken = SignedJWT.parse(originalToken);
            JWTClaimsSet originalPayload = parsedToken.getJWTClaimsSet();
            JWTClaimsSet.Builder modifiedPayloadBuilder = new JWTClaimsSet.Builder(originalPayload);
            modifiedPayloadBuilder.claim("tenant_roles", List.of(Map.of("participant_code", "tes-hcx", "role", "admin")));
            JWTClaimsSet modifiedPayload = modifiedPayloadBuilder.build();
            SignedJWT newToken = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), modifiedPayload);
            newToken.sign(signer);
            Map<String,Object> accessMap = new HashMap<>();
            accessMap.put("access_token",newToken.serialize());
            return accessMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/participant/generate/token")
    public ResponseEntity<Object> participantToken(@RequestBody MultiValueMap<String, String> requestBody){
        try{
             AccessTokenResponse accessTokenResponse = keycloackService.generateToken(requestBody,participantRealmUrl);
             String modifiedAccessToken = keycloackService.modifyToken(accessTokenResponse.getToken(),requestBody.getFirst("username"));
             Map<String,Object> response = keycloackService.getResponse(accessTokenResponse,modifiedAccessToken);
             return ResponseEntity.ok(response);
        } catch (Exception e){
             return exceptionHandler(new Response(),e);
        }
    }

    @PostMapping("/user/generate/token")
    public ResponseEntity<Object> userToken(@RequestBody MultiValueMap<String, String> requestBody){
        try{
            AccessTokenResponse accessTokenResponse = keycloackService.generateToken(requestBody,userRealmUrl);
            String modifiedAccessToken = keycloackService.modifyToken(accessTokenResponse.getToken(),requestBody.getFirst("username"));
            Map<String,Object> response = keycloackService.getResponse(accessTokenResponse,modifiedAccessToken);
            return ResponseEntity.ok(response);
        } catch (Exception e){
            return exceptionHandler(new Response(),e);
        }
    }

}



