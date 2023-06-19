package org.swasth.hcx.controllers.v1;

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
import org.swasth.hcx.service.TokenService;

import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(VERSION_PREFIX)
public class TokenController extends BaseController {

    @Value("${keycloak.user-realm-url}")
    private String userRealmUrl;

    @Value("${keycloak.participant-realm-url}")
    private String participantRealmUrl;

    @Autowired
    private TokenService tokenService;

    @PostMapping(PARTICIPANT_GENERATE_TOKEN)
    public ResponseEntity<Object> participantToken(@RequestBody MultiValueMap<String, String> requestBody){
        try{
             Map<String,Object> response = tokenService.getToken(requestBody,"src/main/resources/participant_realm.der",participantRealmUrl);
             return tokenService.getSuccessResponse(response);
        } catch (Exception e){
             return exceptionHandler(new Response(),e);
        }
    }

    @PostMapping(USER_GENERATE_TOKEN)
    public ResponseEntity<Object> userToken(@RequestBody MultiValueMap<String, String> requestBody){
        try{
            Map<String,Object> response = tokenService.getToken(requestBody,"src/main/resources/user_realm.der",userRealmUrl);
            return tokenService.getSuccessResponse(response);
        } catch (Exception e){
            return exceptionHandler(new Response(),e);
        }
    }

}



