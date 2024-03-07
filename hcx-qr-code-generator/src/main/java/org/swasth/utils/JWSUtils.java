package org.swasth.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

public class JWSUtils {
    public static String generate(Map<String, Object> headers, Map<String, Object> payload, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKeyDecoded = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyDecoded);
        PrivateKey rsaPrivateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
        return Jwts.builder().setHeader(headers).setClaims(payload).signWith(SignatureAlgorithm.RS256, rsaPrivateKey).compact();
    }
}
