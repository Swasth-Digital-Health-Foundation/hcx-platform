package org.swasth.apigateway.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Verification;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;


/**
 * Builds the JWT Verifier instance used for verifying JWT tokens
 *
 * Supported types are HS256, HS384, HS512, RS256, RS384, RS512
 */
@Component
@ConditionalOnBean(JwtConfigs.class)
public class JWTVerifierFactory {

    private final JwtConfigs jwtConfigs;

    @Autowired
    public JWTVerifierFactory(JwtConfigs jwtConfigs) {
        this.jwtConfigs = jwtConfigs;
    }

    @Bean
    public JWTVerifier create()
            throws JwkException, IOException, InvalidKeySpecException, NoSuchAlgorithmException {

        Verification verifier = null;
        if(jwtConfigs.getKey() != null && jwtConfigs.getType() != null){

            if(jwtConfigs.getType().startsWith("HS")){
                if(jwtConfigs.getType().equalsIgnoreCase("HS256")){
                    verifier = JWT.require(Algorithm.HMAC256(jwtConfigs.getKey()));
                }
                else if(jwtConfigs.getType().equalsIgnoreCase("HS384")){
                    verifier = JWT.require(Algorithm.HMAC384(jwtConfigs.getKey()));
                }
                else if(jwtConfigs.getType().equalsIgnoreCase("HS512")){
                    verifier = JWT.require(Algorithm.HMAC512(jwtConfigs.getKey()));
                }
                else{
                    throw new JwkException("Invalid key type, supported HMAC methods are HS256, HS384 & HS512!");
                }
            }
            else {
                PublicKey key = this.parsePublicKey(jwtConfigs.getKey());
                if (jwtConfigs.getType().equalsIgnoreCase("RS256")) {
                    verifier = JWT.require(Algorithm.RSA256((RSAPublicKey) key, null));
                } else if (jwtConfigs.getType().equalsIgnoreCase("RS384")) {
                    verifier = JWT.require(Algorithm.RSA384((RSAPublicKey) key, null));
                } else if (jwtConfigs.getType().equalsIgnoreCase("RS512")) {
                    verifier = JWT.require(Algorithm.RSA512((RSAPublicKey) key, null));
                }
                else{
                    throw new JwkException("Invalid key type, supported RSA methods are RS256, RS384 & RS512!");
                }
            }

        }
        else if (jwtConfigs.getJwkUrl() != null){
            UrlJwkProvider urlJwkProvider = new UrlJwkProvider(new URL(jwtConfigs.getJwkUrl()));
            Jwk jwk = urlJwkProvider.getAll().get(0);
            verifier = JWT.require(Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null));
        }
        else{
            throw new JwkException(("Invalid JWT configuration."));
        }
        if(!ObjectUtils.isEmpty(jwtConfigs.getIssuer())){
            verifier.withIssuer(jwtConfigs.getIssuer());
        }
        if(!ObjectUtils.isEmpty(jwtConfigs.getAudience())){
            verifier.withAudience(jwtConfigs.getAudience());
        }
        if(!ObjectUtils.isEmpty(jwtConfigs.getAllowedSkew())){
            verifier.acceptLeeway(Long.parseLong(jwtConfigs.getAllowedSkew()));
        }
        return verifier.build();
    }

    public PublicKey parsePublicKey(@Value("${jwt.key}") String publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicBytes = Base64.decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(keySpec);
    }
}