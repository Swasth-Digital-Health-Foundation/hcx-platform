package org.swasth.hcx.controllers.v1;

import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.Response;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.service.KeycloackService;

import java.util.Map;

import static org.swasth.common.utils.Constants.VERSION_PREFIX;

@RestController()
@RequestMapping(VERSION_PREFIX)
public class KeycloakController extends BaseController {

    @Value("${keycloak.user-realm-url}")
    private String userRealmUrl;

    @Value("${keycloak.participant-realm-url}")
    private String participantRealmUrl;

    @Autowired
    private KeycloackService keycloackService;

    @PostMapping("/participant/generate/token")
    public ResponseEntity<Object> participantToken(@RequestBody MultiValueMap<String, String> requestBody){
        try{
             AccessTokenResponse accessTokenResponse = keycloackService.generateToken(requestBody,participantRealmUrl);
             String modifiedAccessToken = keycloackService.modifyToken(accessTokenResponse.getToken(),requestBody.getFirst("username"), "participant_realm.der");
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
            String modifiedAccessToken = keycloackService.modifyToken(accessTokenResponse.getToken(),requestBody.getFirst("username"), "user_realm.der");
            Map<String,Object> response = keycloackService.getResponse(accessTokenResponse,modifiedAccessToken);
            return ResponseEntity.ok(response);
        } catch (Exception e){
            return exceptionHandler(new Response(),e);
        }
    }

}



