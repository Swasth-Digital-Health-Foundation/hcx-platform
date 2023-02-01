package org.swasth.hcx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swasth.ICloudService;

import java.util.Map;

import static org.swasth.common.utils.Constants.ENCRYPTION_CERT;
import static org.swasth.common.utils.Constants.SIGNING_CERT_PATH;

@Service
public class ParticipantService {

    @Autowired
    private ICloudService cloudClient;
    @Value("${certificates.bucketName}")
    private String bucketName;

    public void getCertificatesUrl(Map<String, Object> requestBody, String participantCode, String key) {
        if (requestBody.getOrDefault(key, "").toString().startsWith("-----BEGIN CERTIFICATE-----") && requestBody.getOrDefault(key, "").toString().endsWith("-----END CERTIFICATE-----")) {
            String signingCertUrl = participantCode + "/signing_cert_path.pem";
            String encryptionCertUrl = participantCode + "/encryption_cert_path.pem";
            String signingCert = getCertificateData(requestBody, SIGNING_CERT_PATH);
            String encryptionCert = getCertificateData(requestBody, ENCRYPTION_CERT);
            cloudClient.putObject(participantCode, bucketName);
            cloudClient.putObject(bucketName, signingCertUrl, signingCert);
            cloudClient.putObject(bucketName, encryptionCertUrl, encryptionCert);
            requestBody.put(SIGNING_CERT_PATH, cloudClient.getUrl(bucketName, signingCertUrl).toString());
            requestBody.put(ENCRYPTION_CERT, cloudClient.getUrl(bucketName, encryptionCertUrl).toString());
        }
    }

    public String getCertificateData(Map<String, Object> requestBody, String key) {
        return requestBody.getOrDefault(key, "").toString().replace(" ", "\n").replace("-----BEGIN\nCERTIFICATE-----", "-----BEGIN CERTIFICATE-----").replace("-----END\nCERTIFICATE-----", "-----END CERTIFICATE-----");
    }
}
