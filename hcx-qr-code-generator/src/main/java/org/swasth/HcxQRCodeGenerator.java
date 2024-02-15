package org.swasth;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.swasth.service.EncDeCode;
import org.swasth.utils.JWSUtils;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

public class HcxQRCodeGenerator {
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            String json = args[0];
            Gson gson = new Gson();
            Map<String, Object> map = gson.fromJson(json, HashMap.class);
            System.out.println("Map received from command line argument:");
            generateJWSToken((Map<String, Object>) map.get("payload"), (String) map.get("signingPrivateKey"));
        } else {
            System.out.println("No input to process");
        }
    }

    private static String getPrivateKey(String privateKey) {
        privateKey = privateKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        return privateKey;
    }

    public static Map<String, Object> generateJWSToken(Map<String, Object> requestBody, String privateKey) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        String jwsToken = JWSUtils.generate(headers, requestBody, HcxQRCodeGenerator.getPrivateKey(privateKey));
        Map<String, Object> payload = createVerifiableCredential(requestBody, jwsToken);
        generateQRCode(EncDeCode.encodePayload(payload));
        return payload;
    }

    public static Map<String, Object> createVerifiableCredential(Map<String, Object> payload, String proofValue) throws Exception {
        Map<String, Object> credential = new HashMap<>();
        credential.put("@context", Arrays.asList(
                "https://www.w3.org/2018/credentials/v1",
                "https://www.w3.org/2018/credentials/examples/v1"
        ));
        credential.put("id", "http://hcxprotocol.io/credentials/3732");
        credential.put("type", Collections.singletonList("VerifiableCredential"));
        credential.put("issuer", "https://hcxprotocol.io/participant/565049");
        credential.put("issuanceDate", LocalDateTime.now());
        credential.put("expirationDate", LocalDateTime.now().plusMonths(12));
        credential.put("preferredHCXPath", "http://hcx.swasth.app/api/v0.8/");
        Map<Object, Object> credentialSubject = new HashMap<>();
        credentialSubject.put("id", UUID.randomUUID());
        credentialSubject.put("payload", payload);
        credential.put("credentialSubject", credentialSubject);
        Map<String, Object> proof = new HashMap<>();
        proof.put("type", "Ed25519Signature2020");
        proof.put("created", LocalDateTime.now());
        proof.put("verificationMethod", "https://hcxprotocol.io/issuers/565049#key-1");
        proof.put("proofPurpose", "assertionMethod");
        proof.put("proofValue", proofValue);
        credential.put("proof", proof);
        return credential;
    }

    private static void generateQRCode(String content) throws Exception {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 150, 150);
        String currentDir = System.getProperty("user.dir");
        Path path = FileSystems.getDefault().getPath(currentDir + "/qr_code.png");
        MatrixToImageWriter.writeToPath(matrix, "PNG", path);
        System.out.println("QR code image generated and saved to: " + path.toAbsolutePath());
    }
}
