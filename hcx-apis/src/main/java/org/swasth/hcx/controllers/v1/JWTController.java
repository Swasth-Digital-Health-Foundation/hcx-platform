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
import org.swasth.hcx.service.JWTService;

import java.util.Collections;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(VERSION_PREFIX)
public class JWTController extends BaseController {

    @Value("${keycloak.user-realm-url}")
    private String userRealmUrl;

    @Value("${keycloak.participant-realm-url}")
    private String participantRealmUrl;

    @Value("${keycloak.protocol-api-access-realm-url}")
    private String protocolApiAccessRealmUrl;

    @Value("${keycloak.participant-realm-private-key-path:classpath:participant_realm_private_key.der}")
    private String participantRealmKeyPath;

    @Value("${keycloak.user-realm-private-key-path:classpath:user_realm_private_key.der}")
    private String userRealmKeyPath;

    @Value("${keycloak.protocol-api-access-private-key-path:classpath:api_access_realm_private_key.der}")
    private String protocolApiAccessRealmKeyPath;

    @Value("${keycloak.user-client-id}")
    private String userClientId;

    @Value("${keycloak.participant-client-id}")
    private String participantClientId;

    @Value("${keycloak.protocol-api-access-client-id}")
    private String protocolApiAccessClientId;

    @Autowired
    private JWTService jwtService;

    @PostMapping(PARTICIPANT_GENERATE_TOKEN)
    public ResponseEntity<Object> participantToken(@RequestBody MultiValueMap<String, String> requestBody){
        try {
            Map<String, Object> response;
            if (requestBody.containsKey(PARTICIPANT_CODE)) {
                jwtService.validate(requestBody);
                requestBody.put(USER_ID, Collections.singletonList(requestBody.getFirst(USERNAME)));
                requestBody.put(USERNAME, Collections.singletonList(requestBody.getFirst(PARTICIPANT_CODE) + ":" + requestBody.getFirst(USERNAME)));
                response = jwtService.getToken(requestBody, protocolApiAccessRealmKeyPath, protocolApiAccessRealmUrl, protocolApiAccessClientId);
            } else {
                response = jwtService.getToken(requestBody, participantRealmKeyPath, participantRealmUrl, participantClientId);
            }
            return jwtService.getSuccessResponse(response);
        } catch (Exception e){
             return exceptionHandler(new Response(),e);
        }
    }

    @PostMapping(USER_GENERATE_TOKEN)
    public ResponseEntity<Object> userToken(@RequestBody MultiValueMap<String, String> requestBody){
        try{
            Map<String,Object> response = jwtService.getToken(requestBody, userRealmKeyPath, userRealmUrl, userClientId);
            return jwtService.getSuccessResponse(response);
        } catch (Exception e){
            return exceptionHandler(new Response(),e);
        }
    }

}



