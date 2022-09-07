package org.swasth.common.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

public class JWTUtils {

    public boolean isValidSignature(String payload, String publicKeyUrl) throws Exception {
        String certificate = IOUtils.toString(new URL(publicKeyUrl), StandardCharsets.UTF_8.toString());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream stream = new ByteArrayInputStream(certificate.getBytes()); //StandardCharsets.UTF_8
        Certificate cert = cf.generateCertificate(stream);
        PublicKey publicKey = cert.getPublicKey();
        String[] parts = payload.split("\\.");
        String data = parts[0] + "." + parts[1];
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes());
        byte[] decodedSignature = Base64.getUrlDecoder().decode(parts[2]);
        return sig.verify(decodedSignature);
    }

    public Long getCertificateExpiry(String publicKeyUrl) throws CertificateException, IOException {
        X509Certificate certificate = (X509Certificate) CertificateFactory
                .getInstance("X509")
                .generateCertificate(new ByteArrayInputStream(IOUtils.toString(new URL(publicKeyUrl), StandardCharsets.UTF_8.toString()).getBytes()));
        return certificate.getNotAfter().toInstant().toEpochMilli();
    }

    public String generateJWS(Map<String,Object> headers, Map<String,Object> payload, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
       byte[] privateKeyDecoded = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyDecoded);
        PrivateKey rsaPrivateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
        return Jwts.builder().setHeader(headers).setClaims(payload).signWith(SignatureAlgorithm.RS256, rsaPrivateKey).compact();
    }

}
