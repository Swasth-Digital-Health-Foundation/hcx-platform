package org.swasth.service;

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
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;

public class VerifyQrCode {
    private static final Logger logger = Logger.getLogger(String.valueOf(VerifyQrCode.class));

    public static Map<String, Object> getToken(Map<String, Object> payload) throws CertificateException, IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String publicKeyUrl = "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem";
        Map<String, Object> token = (Map<String, Object>) payload.get("proof");
        if (token.containsKey("proofValue")) {
            String jwsToken = (String) token.get("proofValue");
            boolean isSignatureValid = isValidSignature(jwsToken, publicKeyUrl);
           logger.info(isSignatureValid + " Valid Signature");
        } else logger.info("proofValue is empty or null");
        return token;
    }

    public static boolean isValidSignature(String payload, String publicKeyUrl) throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String certificate = IOUtils.toString(new URL(publicKeyUrl), StandardCharsets.UTF_8);
        byte[] certificateBytes = certificate.getBytes(StandardCharsets.UTF_8);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream stream = new ByteArrayInputStream(certificateBytes);
        Certificate cert = cf.generateCertificate(stream);
        PublicKey publicKey = cert.getPublicKey();
        String[] parts = payload.split("\\.");
        String data = String.join(".", Arrays.copyOfRange(parts, 0, 2));
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] decodedSignature = Base64.getUrlDecoder().decode(parts[2]);
        return sig.verify(decodedSignature);
    }

}
