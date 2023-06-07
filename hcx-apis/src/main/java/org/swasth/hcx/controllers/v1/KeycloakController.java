package org.swasth.hcx.controllers.v1;

import com.auth0.jwt.JWTVerifier;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.keycloak.TokenVerifier;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
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
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.GENERATE_TOKEN;
import static org.swasth.common.utils.Constants.VERSION_PREFIX;

@RestController()
@RequestMapping(VERSION_PREFIX)
public class KeycloakController {


    private final JWTVerifier jwtVerifier;

    public KeycloakController(JWTVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }


    @PostMapping(GENERATE_TOKEN)
    public ResponseEntity<Object> generateToken(@RequestBody MultiValueMap<String, String> requestBody) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", requestBody.getFirst("grant_type"));
        map.add("client_id", requestBody.getFirst("client_id"));
        map.add("client_secret", "c4f23c80-96d1-4f39-9784-8a7cb1f61e7a");
        map.add("username", requestBody.getFirst("username"));
        map.add("password", requestBody.getFirst("password"));
        String keycloakServerUrl = "http://dev-hcx.swasth.app/auth/realms/swasth-health-claim-exchange/protocol/openid-connect/token";
       // String keycloakServerUrl = "http://localhost:8080/auth/realms/sunbird-rc/protocol/openid-connect/token";
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        AccessTokenResponse response = restTemplate.postForEntity(keycloakServerUrl, request, AccessTokenResponse.class).getBody();
        TokenVerifier<AccessToken> verifier = TokenVerifier.create(response.getToken(), AccessToken.class);
        response.getOtherClaims().put("tenant_roles","participant_code");
        String existingToken = response.getToken();
        String swasthPrivateKey = "MIIEpAIBAAKCAQEAitYHlzTku6Eeej0G1ocWOvqlmew15lAs1WXBxUQ+GJq8MoihUsEH1AsLDo7qu1gJClAzzKZnL1jpGl2+230PINYYzRUbmg11c+0VY3UahaqB6p+NyfWUgOXPIASIQsUOSzcQJt7GfnBAD0mdj+/o0l065OJfMbN1bWjtpqGZM//+PJIJjNEkSru6VMujxgTtxNbr16QknWkncKJTr4fGdRURWHLT6ee+go+x9+Ll1aDzS9J+FX+//+C0ORuh5xp5pxOvyu3uIXT18BWAUrX5QdoULNta/qNUBzRQp1AR8sCS/5ZLlToh6B1RQ5A/baXSncZloTdPjzcoKNieZ1ULGQIDAQABAoIBAEh2mZOkOdZsq9mlTJJgA4xZEmIaVa+WHTBsYgyyJ67z5FKD2Z+1vH5CHU1F7uJFesJ1RfQEzkp6H4BxeZJZ3bGHzX4NDFEFwU32anl0v5kQe6qYCItmsFHuoILXmr8u1t0PKlD/aZ7Iu8a+54we7egOXRTlykXIovuuDowwQCRRKixmYPBjonENOQ3hyxrshu3eOWx+s2Lz/ZG/tYVw65dg6jbHN7dWyp+wh6z0EUews23L5psnjkMIgzh/OKNu9mHkmhuXiqiJDNj837OlZidMOQfNWaxGZ4gCrVcMU1vEcDnMPVrPa9ZPMdYfAHdL/8XUsKMExq98tcuTiAWZZgECgYEA5VUz1psKTtzomLILqOerKUZ898eGgvUOTZCIBCXQm6gqlw5hNJkFg/QUGEmYq9txo9XMvXLUU+Pz86EJBIpPr1JydKCo2VaGJbpaP8qDWdhKcfRHzJpnWNlMz2+qpLvuyBz6dkSfqHc697jITP+d87gFcog7/tXaGqY7Hx9XmLkCgYEAmvrphv/uqF6qHi9PMAuDI/y/pvTq729FZdgo/CRRa8oz8cCXPTnBD0PffMf9/fT2CLAear2JRPaHU4squEkFsPuuiavpVvOHVAixxaebjQLkTlMNWxk6eIjKDWVgWHuKK4hcw44G3mctVssIcLa2Z/ZClcnVqGAgxDldwDwFFWECgYEA19bt+BBpjjnJzFTeNnT8GdfWrBmk1sIvWP8rlMPGZ4nJs+v7vtY6Y6un8gfqYe0iMnF5xeA6SE5l/qlUZGqMftTZFj1TTWJq1T1jzWAbacbwofmoTcAQfoXeDenqMCsUnFDxxbfmkFbPL/FAy39VONm8fsQLu1eoc6Z6RFtqRTkCgYAdrsDbYllLVhYvAdCg2xOa6OWsDGySvzUDdIsk/6+4fVnFi6VrzIv1aIJ9W7CB21DkCRdKSlLoKm9wyM69zP6SDyI2q/5c8PeSWLfzq05Xi54+ghmkwQg53bkVJ710NPUZsVxS9/jIz+oXHXvYWqZE8x19otKRFRgxfOs/zj8LAQKBgQC5cmOiGg3GrmTHa6uYDGpwsFXeeFw/vtIQ/o9qZlE73nZdG0vL1NBZwLpy+kNOYAOBWRl8vPSCWsO7K138Tju+Iwvhp9BdrjjDX2Wvsvf86Cy5ib2P332i/3KK5kTad77GFIr/WSQnDx0xwBOYnDaclvtLjZQULoP13/VLKDJuWQ==";
        String privateKey= "-----BEGIN PRIVATE KEY-----\nMIIEpAIBAAKCAQEA8MtvR1J+DlaUt8Zehh6ZRTEjxB4v5ffDGauRdXeyM9//t4W2sU5hjzG4bM1ro5bRauWkjKa9s9Lu+mH4XMPLxaPG+IxlJTDfMfqq1S98pqq5cO5XG80kLKEX1hvLsIOMn3NjtQc9bB7otazEnNyxFDR/1HmIbsJMYtmSZSwd3QStn58j+vt1p3V7Mo4P8KKbiwal+m+4qanu1QH7Pvx4BgwY4XB/NwmkV1ke+s0HKGm4kXf2QCdXWs+ireUPDy+FbdidDFWBtnw6DB5vqZfWbMbTmZ5mnUXaf/WmWera4M/kGOknsQG9p0pYOcM8qw025Uc37NBar1Nqq1rcwjkmRwIDAQABAoIBAEcv7XsJcUjt4dOe6qz9+NQS3f8J6aE0KVK6fStcMqvRajkoZ8VtZbw+t8Y6yLq5SLowoAAQ/35dOtd9BT7vh5urepRadUCUfTe6YqzZ+CER0fcID2qhsDMdgP2EhChwC6/MHksTk94WGTg0ln/FYviVOMFpGKOd/5Utu3D7pOyE9bueSfPane429VOtDnakW6nVO+5tshcAy9T9ts4FvvAYEqPqPyJFUyYjgqFJ3vBWCO77EMzysVZeaxJQ1xDousoAI0+9mCZgYsQMAb//wwMApf0HcTdQO9xuSdHyo3dM+UqVPAM9XHDC3c/YiKE9UA7GJ63KdVUrRwVjKBvsHyECgYEA+why8s4Ft4qjDj+mCDQW/gcoWQMyBWAwDIxY7t+BcAbQ09D/2VHwine9A6j9m2Q9R+4ne7fbLk+bgVcsdOc8y0J3nH0tBjrFk+LQ8N/jv56A+eKtrtXaATa2MrM/Z/ujNh4mlDCCBKLo8jrBXqu+09xFpRrYRRzUH2xPsGJe1lECgYEA9Y8gLIyZwdqj18sQfH1dFrAozFDyxr2KLXfSvNx3pjXd4m4YNqKrtLWY5idMNr18+3c5EXn327U0PkqvcMotyvrV7qhG8IYGknEk7zCjcNCunZ0GLCKSN3w/aKeg0fZ9p1bcOk7/mfcVj98iGbI5Dqtt91g8njLXShMig5QbVRcCgYEAvJnucsmitfDs7ImZXlR0acpK4AVskWlg8CyJrH4zq00Tm/BMKKRNSlsHoTZXw2WePOqKs4LAo5yfwp8SAYqxvCxl3SaWqzWAt8kTHcW4QID/eb56qv50WbEXViQhoSgHyWMBrRh8tqsGxOar3Uq/hkDZ+l+e2N396NXhUS22/uECgYEAsbyjARvTvujOZsdoa3MiCeX/4cNFtmxCs55jPqglQn0C2X3usL0Vo3s97HjNRWHqMOmeuPObX7/MzLmqOu1cL+tXJaNPlZCs+RpELYz7ABFMEnExoohzZQ3dp5aNZOwRDypjxSChCYQ6aySKviat/dw/gCSx25/ZDJjFBpARKIkCgYByP0NunhRkd5GocOtjlXwh/ZuAc7xC3UjtsLwx4z+OQjo4DQHKP7VVMG8Cng/CBgCvTYBDEQJpviX9LAURoH0qKWlZlWBUVq6ilMdEgYN/MrYHaT16vvqrvz8t2s02mZpGbs+2oZHnb91ZulWz2lxar8/PX+94gBo0oYCVYlYi2Q==\n-----END PRIVATE KEY-----";
        Map<String,Object> jwtHeader = JSONUtils.decodeBase64String(existingToken.split("\\.")[0],Map.class);
        Map<String, Object> jwtPayload = JSONUtils.decodeBase64String(existingToken.split("\\.")[1], Map.class);
        jwtPayload.put("tenant_roles","participant_code");
//        String payload = response.getToken();
//        JWTUtils jwtUtils = new JWTUtils();
//        System.out.println("==============its here----------------------");
//        String jwtTok = jwtUtils.generateJWS(jwtHeader,jwtPayload,swasthPrivateKey);
//        System.out.println(jwtTok);
        RSAPrivateKey pkey = getPrivateKey();
        String newToken = sign(existingToken,pkey);
        Map<String,Object> newJwtToken = new HashMap<>();
        newJwtToken.put("access_token",newToken);
        newJwtToken.put("refresh_token",response.getRefreshToken());
        System.out.println(jwtVerifier.verify(newToken));

        return  ResponseEntity.ok(newJwtToken);
    }

    private static String sign(String existingToken,RSAPrivateKey privateKey) {
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(privateKey).parseClaimsJws(existingToken);
        claimsJws.getBody().get("realm_access",Map.class).put("tenant_roles", List.of(Map.of("participant_code","tes-hcx","role","admin")));
        String signedToken = Jwts.builder()
                .setClaims(claimsJws.getBody())
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
        return signedToken;
    }


    private static RSAPrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File file = new File("/mnt/c/Users/ASUS/Documents/keys/swasth-realm/private.der");
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int) file.length()];
        dis.readFully(keyBytes);
        dis.close();
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(spec);
        return privateKey;
    }
}
