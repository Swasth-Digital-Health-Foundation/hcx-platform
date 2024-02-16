package org.swasth.commonscheduler.schedulers;

import com.fasterxml.jackson.core.JsonProcessingException;
import kong.unirest.HttpResponse;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.NotificationUtils;
import org.swasth.common.validation.CertificateRevocation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

@Component
public class CertificateRevocationScheduler extends BaseScheduler {

    private final Logger logger = LoggerFactory.getLogger(CertificateRevocationScheduler.class);

    @Autowired
    private RegistryService registryService;

    @Value("${hcx.participantCode}")
    private String hcxParticipantCode;

    @Value("${hcx.privateKey}")
    private String hcxPrivateKey;

    @Value("${kafka.topic.notification}")
    private String notifyTopic;

    @Value("${topicCode.revoked-certificate}")
    private String certificateRevokedTopicCode;
    @Value("${topicCode.invalid-certificate}")
    private String invalidCertificateTopicCode;

    public void process() throws Exception {
        logger.info("Certificate Revocation validation scheduler started");
        List<Map<String, Object>> participants = registryService.getDetails("{ \"filters\": {} }");
        processCertificate(Constants.ENCRYPTION_CERT, participants);
        processCertificate(Constants.SIGNING_CERT_PATH, participants);
        logger.info("Certificate revocation validation scheduler ended");
    }

    private void processCertificate(String certKey, List<Map<String, Object>> participants) throws Exception {
        Map<String, List<String>> processedMap = new HashMap<>();
        for (Map<String, Object> participant : participants) {
            if (participant.containsKey(certKey)) {
                String certificatePath = (String) participant.get(certKey);
                processedMap = processParticipant(certificatePath, participant);
            }
        }
        List<String> invalidCertificates = processedMap.getOrDefault("invalidCertificates", new ArrayList<>());
        List<String> revokedCertificates = processedMap.getOrDefault("revokedParticipantCodes", new ArrayList<>());
        generateEvent(invalidCertificates, getTemplateMessage(invalidCertificateTopicCode), invalidCertificateTopicCode);
        generateEvent(revokedCertificates, getTemplateMessage(certificateRevokedTopicCode), certificateRevokedTopicCode);
        logger.info("Total number of participants with revoked {} certificates: {}", certKey, invalidCertificates.size());
        logger.info("Total number of invalid {} certificates: {}", certKey, revokedCertificates.size());
    }

    private void generateEvent(List<String> participantCodes, String message, String topiCode) throws Exception {
        Calendar cal = Calendar.getInstance();
        String event = eventGenerator.createNotifyEvent(topiCode, hcxParticipantCode, Constants.PARTICIPANT_CODE, participantCodes, cal.getTime().toInstant().toEpochMilli(), message, hcxPrivateKey);
        kafkaClient.send(notifyTopic, Constants.NOTIFICATION, event);
        logger.info("Notify event is pushed to kafka: {}", event);
    }

    private String getTemplateMessage(String topicCode) throws JsonProcessingException {
        return (String) JSONUtils.deserialize((String) (NotificationUtils.getNotification(topicCode).get(Constants.TEMPLATE)), Map.class).get(Constants.MESSAGE);
    }


    private boolean checkRevocationStatus(X509Certificate x509Certificate) throws OCSPException, CertificateException, IOException, ClientException, OperatorCreationException, CRLException {
        CertificateRevocation certificateRevocation = new CertificateRevocation(x509Certificate);
        return certificateRevocation.checkStatus();
    }

    private X509Certificate parseCertificateFromURL(String urlString) throws IOException, ClientException {
        URL url = new URL(urlString);
        try (InputStream inputStream = url.openStream()) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(inputStream);
        } catch (Exception e) {
            throw new ClientException(ErrorCodes.ERR_INVALID_CERTIFICATE, "Error parsing certificate from the URL");
        }
    }

    private Map<String, List<String>> processParticipant(String certificatePath, Map<String, Object> participant) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> revokedParticipantCodes = new ArrayList<>();
        List<String> invalidCertificates = new ArrayList<>();
        String participantCode = (String) participant.get(Constants.PARTICIPANT_CODE);
        if (certificatePath != null) {
            try {
                X509Certificate x509Certificate = parseCertificateFromURL(certificatePath);
                if (checkRevocationStatus(x509Certificate)) {
                    revokedParticipantCodes.add(participantCode);
                    boolean response = registryService.updateStatusOnCertificateRevocation((String) participant.get(Constants.OSID));
                    if (response) {
                        logger.info("Participant status set to 'Inactive' for participant with code: {}", participantCode);
                    }
                }
            } catch (Exception e) {
                invalidCertificates.add(participantCode);
                logger.error("Invalid certificate for participant with code {}: {}", participantCode, e.getMessage());
            }
        }
        result.put("revokedParticipantCodes", revokedParticipantCodes);
        result.put("invalidCertificates", invalidCertificates);
        return result;
    }
}
