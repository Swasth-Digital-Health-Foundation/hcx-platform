package org.swasth.hcx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kong.unirest.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.*;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.x509.extension.X509ExtensionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.swasth.ICloudService;
import org.swasth.common.dto.RegistryResponse;
import org.swasth.common.dto.Token;
import org.swasth.common.exception.*;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;
import org.swasth.postgresql.IDatabaseService;
import org.swasth.redis.cache.RedisCache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;

import static org.swasth.common.response.ResponseMessage.*;
import static org.swasth.common.utils.Constants.*;

@Service
public class ParticipantService extends BaseRegistryService {

    private static final Logger logger = LoggerFactory.getLogger(ParticipantService.class);
    @Value("${certificates.bucketName}")
    private String bucketName;

    @Value("${postgres.onboardingOtpTable}")
    private String onboardOtpTable;

    @Value("${postgres.onboardingTable}")
    private String onboardingTable;
    @Value("${registry.organisation-api-path}")
    private String registryOrgnisationPath;
    @Value("${certificate.trusted-cas}")
    private List<String> trustedCAs ;
    @Value("${certificate.key-size}")
    private List<Integer> allowedCertificateKeySize;

    @Autowired
    protected IDatabaseService postgreSQLClient;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private ICloudService cloudClient;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private Environment env;

    public RegistryResponse create(Map<String, Object> requestBody, HttpHeaders header, String code) throws Exception {
        HttpResponse<String> response = invite(requestBody, registryOrgnisationPath);
        if (response.getStatus() == 200) {
            generateCreateAudit(code, PARTICIPANT_CREATE, requestBody, CREATED, getUserFromToken(header));
        }
        return responseHandler(response, code, ORGANISATION);
    }

    public RegistryResponse update(Map<String, Object> requestBody, Map<String, Object> registryDetails, HttpHeaders header, String code) throws Exception {
        HttpResponse<String> response = update(requestBody, registryDetails, registryOrgnisationPath);
        if (response.getStatus() == 200) {
            deleteCache(code);
            String status = (String) registryDetails.get(REGISTRY_STATUS);
            generateUpdateAudit(code, PARTICIPANT_UPDATE, requestBody, status, (String) requestBody.getOrDefault(REGISTRY_STATUS, status), getUpdatedProps(requestBody, registryDetails), getUserFromToken(header));
        }
        return responseHandler(response, code, ORGANISATION);
    }

    public RegistryResponse search(Map<String, Object> requestBody) throws ServerException, AuthorizationException, ClientException, ResourceNotFoundException, JsonProcessingException {
        return search(requestBody, registryOrgnisationPath,ORGANISATION);
    }

    public Map<String, Object> read(String code) throws Exception {
        ResponseEntity<Object> searchResponse = getSuccessResponse(search(JSONUtils.deserialize(getRequestBody(code), Map.class)));
        RegistryResponse searchResp = (RegistryResponse) searchResponse.getBody();
        logger.debug("Read participant is completed");
        if (searchResp != null && !searchResp.getParticipants().isEmpty())
            return (Map<String, Object>) searchResp.getParticipants().get(0);
        else
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, "Please provide valid participant code");

    }

    public RegistryResponse delete(Map<String, Object> registryDetails, HttpHeaders header, String code) throws Exception {
        HttpResponse<String> response = delete(registryDetails, registryOrgnisationPath);
        if (response.getStatus() == 200) {
            deleteCache(code);
            generateUpdateAudit(code, PARTICIPANT_DELETE, Collections.emptyMap(), (String) registryDetails.get(REGISTRY_STATUS), INACTIVE, Collections.emptyList(), getUserFromToken(header));
        }
        return responseHandler(response, code, ORGANISATION);
    }

    private void getCertificates(Map<String, Object> requestBody, String participantCode, String key) {
        if (requestBody.getOrDefault(key, "").toString().startsWith("-----BEGIN CERTIFICATE-----") && requestBody.getOrDefault(key, "").toString().endsWith("-----END CERTIFICATE-----")) {
            String certificateData = requestBody.getOrDefault(key, "").toString().replace(" ", "\n").replace("-----BEGIN\nCERTIFICATE-----", "-----BEGIN CERTIFICATE-----").replace("-----END\nCERTIFICATE-----", "-----END CERTIFICATE-----");
            cloudClient.putObject(participantCode, bucketName);
            cloudClient.putObject(bucketName, participantCode + "/" + key + ".pem", certificateData);
            requestBody.put(key, cloudClient.getUrl(bucketName, participantCode + "/" + key + ".pem").toString());
        }
    }

    private void deleteCache(String code) throws Exception {
        if (redisCache.isExists(code))
            redisCache.delete(code);
    }

    public void getCertificatesUrl(Map<String, Object> requestBody, String code) {
        getCertificates(requestBody, code, ENCRYPTION_CERT);
        if (requestBody.containsKey(SIGNING_CERT_PATH))
            getCertificates(requestBody, code, SIGNING_CERT_PATH);
    }

    public Map<String, Object> getParticipant(String code) throws Exception {
        ResponseEntity<Object> searchResponse = getSuccessResponse(search(JSONUtils.deserialize(getRequestBody(code), Map.class)));
        RegistryResponse registryResponse = (RegistryResponse) Objects.requireNonNull(searchResponse.getBody());
        if (registryResponse.getParticipants().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, INVALID_PARTICIPANT_CODE);
        return (Map<String, Object>) registryResponse.getParticipants().get(0);
    }

    public void validate(Map<String, Object> requestBody, boolean isCreate) throws ClientException, CertificateException, IOException {
        List<String> notAllowedUrls = env.getProperty(HCX_NOT_ALLOWED_URLS, List.class, new ArrayList<String>());
        if (isCreate) {
            if (!requestBody.containsKey(ROLES) || !(requestBody.get(ROLES) instanceof ArrayList) || ((ArrayList<String>) requestBody.get(ROLES)).isEmpty())
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, INVALID_ROLES_PROPERTY);
            else if (((ArrayList<String>) requestBody.get(ROLES)).contains(PAYOR) && !requestBody.containsKey(SCHEME_CODE))
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, MISSING_SCHEME_CODE);
            else if (!((ArrayList<String>) requestBody.get(ROLES)).contains(PAYOR) && requestBody.containsKey(SCHEME_CODE))
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, UNKNOWN_PROPERTY);
            else if (notAllowedUrls.contains(requestBody.get(ENDPOINT_URL)))
                throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, INVALID_END_POINT);
            else if (!requestBody.containsKey(PRIMARY_EMAIL) || !(requestBody.get(PRIMARY_EMAIL) instanceof String)
                    || !EmailValidator.getInstance().isValid((String) requestBody.get(PRIMARY_EMAIL)))
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, INVALID_EMAIL);
        } else {
            if (notAllowedUrls.contains(requestBody.getOrDefault(ENDPOINT_URL, "")))
                throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, INVALID_END_POINT);
            if (requestBody.containsKey(ENCRYPTION_CERT))
                requestBody.put(ENCRYPTION_CERT_EXPIRY, jwtUtils.getCertificateExpiry((String) requestBody.get(ENCRYPTION_CERT)));
            if (requestBody.containsKey(SIGNING_CERT_PATH))
                requestBody.put(SIGNING_CERT_PATH_EXPIRY, jwtUtils.getCertificateExpiry((String) requestBody.get(SIGNING_CERT_PATH)));
        }
    }

    public void validateCertificates(Map<String, Object> requestBody) throws ClientException, CertificateException, IOException {
        if (!requestBody.containsKey(ENCRYPTION_CERT) || !(requestBody.get(ENCRYPTION_CERT) instanceof String))
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, INVALID_ENCRYPTION_CERT);
        if (requestBody.containsKey(SIGNING_CERT_PATH))
            requestBody.put(SIGNING_CERT_PATH_EXPIRY, jwtUtils.getCertificateExpiry((String) requestBody.get(SIGNING_CERT_PATH)));
        requestBody.put(ENCRYPTION_CERT_EXPIRY, jwtUtils.getCertificateExpiry((String) requestBody.get(ENCRYPTION_CERT)));
    }

    private String getRequestBody(String code) {
        return "{ \"filters\": { \"participant_code\": { \"eq\": \" " + code + "\" } } }";
    }

    private void generateCreateAudit(String code, String action, Map<String, Object> requestBody, String registryStatus, String createdBy) throws Exception {
        Map<String,Object> edata = getEData(registryStatus, "", Collections.emptyList());
        edata.put("createdBy", createdBy);
        Map<String,Object> event = eventGenerator.createAuditLog(code, PARTICIPANT, getCData(action, requestBody), edata);
        eventHandler.createParticipantAudit(event);
    }
    private void generateUpdateAudit(String code, String action, Map<String, Object> requestBody, String prevStatus, String currentStatus, List<String> props, String updatedBy) throws Exception {
        Map<String,Object> event = eventGenerator.createAuditLog(code, PARTICIPANT, getCData(action, requestBody), getEData(currentStatus, prevStatus, props, updatedBy));
        eventHandler.createParticipantAudit(event);
    }

    public void authorizeEntity(String authToken, String participantCode, String sub) throws ClientException, JsonProcessingException {
        Token token = new Token(authToken);
        if (token.getRoles().contains(ADMIN_ROLE)) {
            return;
        } else if (validateRoles(token.getRoles())){
            if(!StringUtils.equalsIgnoreCase(token.getSubject(), sub))
                throw new ClientException(ErrorCodes.ERR_ACCESS_DENIED, "Invalid authorization token");
        } else if (StringUtils.equals(token.getEntityType(), "User")) {
            boolean result = false;
            for(Map<String, String> roleMap: token.getTenantRoles()){
                if(StringUtils.equals(roleMap.get(PARTICIPANT_CODE), participantCode) && (StringUtils.equals(roleMap.get(ROLE), ADMIN) || StringUtils.equals(roleMap.get(ROLE), CONFIG_MANAGER))) {
                    result = true;
                }
            }
            if(!result){
                throw new ClientException(ErrorCodes.ERR_ACCESS_DENIED, "User does not have permissions to update the participant details");
            }
        }
    }

    private boolean validateRoles(List<String> tokenRoles) {
        for(String role: tokenRoles) {
            if(Constants.PARTICIPANT_ROLES.contains(role))
                return true;
        }
        return false;
    }


    private static <K, V> List<K> getUpdatedProps(Map<K, V> map1, Map<K, V> map2) {
        Map<K, V> differentEntries = new HashMap<>();
        for (Map.Entry<K, V> entry : map1.entrySet()) {
            K key = entry.getKey();
            V value1 = entry.getValue();
            V value2 = map2.get(key);

            if (value2 == null || !value2.equals(value1)) {
                differentEntries.put(key, value1);
            }
        }
        return new ArrayList<>(differentEntries.keySet());
    }

    public void validateAndProcessCertificate(Map<String, Object> requestBody, String certKey) throws ClientException {
        if (requestBody.containsKey(certKey)) {
            certificateValidations((String) requestBody.get(certKey));
        }
    }

    public void certificateValidations(String certificate) throws ClientException {
        try {
            X509Certificate x509Certificate;
            if (certificate.startsWith("-----BEGIN CERTIFICATE-----") && certificate.endsWith("-----END CERTIFICATE-----")) {
                x509Certificate = parseCertificateFromString(certificate);
            } else {
                x509Certificate = parseCertificateFromURL(certificate);
            }
            // Validate Certificate Dates
            x509Certificate.checkValidity();
            X509CertificateHolder certHolder = new X509CertificateHolder(x509Certificate.getEncoded());
            RDN[] issuerOrganizationRDNs = certHolder.getIssuer().getRDNs(org.bouncycastle.asn1.x500.X500Name.getDefaultStyle().attrNameToOID("O"));
            String issuerOrganizationName = "";
            if (issuerOrganizationRDNs != null && issuerOrganizationRDNs.length > 0) {
                issuerOrganizationName = String.valueOf(issuerOrganizationRDNs[0].getFirst().getValue());
            }
            List<String> trustedCAListWithReplacedComma = trustedCAs.stream()
                    .map(s -> s.replace("#COMMA#", ","))
                    .collect(Collectors.toList());

            // Validate that the issuing certificate authority is in the trusted CA list
            if (!trustedCAListWithReplacedComma.contains(issuerOrganizationName)) {
                throw new ClientException(ErrorCodes.ERR_INVALID_CERTIFICATE, "The issuing certificate authority should be trusted");
            }
            // Validate that the certificate key size is above 2048 bits
            int keySize = ((RSAPublicKey) x509Certificate.getPublicKey()).getModulus().bitLength();
            if (!allowedCertificateKeySize.contains(keySize)) {
                throw new ClientException(ErrorCodes.ERR_INVALID_CERTIFICATE, String.format("Certificate must have a minimum key size of 2048 bits. Current key size: %d bits.", keySize));
            }
            Map<String, Object> certificateAccessInformation = certificateAccessInformation(x509Certificate);
            if(!certificateAccessInformation.isEmpty()){
                OCSPReq ocspReq = generateOCSPRequest(parseCertificateFromURL(certificateAccessInformation.get(ISSUER_CERTIFICATE).toString()), x509Certificate);
                OCSPResp ocSpResp = sendOCSPRequest(ocspReq, certificateAccessInformation.get(OCSP_URL).toString());
                if (!checkRevocationStatus(ocSpResp)) {
                    throw new ClientException(ErrorCodes.ERR_INVALID_CERTIFICATE, "The certificate has been revoked or is invalid.");
                }
            }
        } catch (Exception e) {
            throw new ClientException(ErrorCodes.ERR_INVALID_CERTIFICATE, e.getMessage());
        }
    }

    private X509Certificate parseCertificateFromString(String certificate) throws CertificateException {
        String cleanedCertificate = certificate
                .replaceAll("-----BEGIN CERTIFICATE-----", "")
                .replaceAll("-----END CERTIFICATE-----", "")
                .replaceAll("\\s", "");
        byte[] certificateBytes = Base64.getDecoder().decode(cleanedCertificate);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        InputStream inputStream = new ByteArrayInputStream(certificateBytes);
        return (X509Certificate) certificateFactory.generateCertificate(inputStream);
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

    private Map<String, Object> certificateAccessInformation(X509Certificate x509Certificate) throws IOException {
        byte[] extVal = x509Certificate.getExtensionValue(Extension.authorityInfoAccess.getId());
        Map<String, Object> responseMap = new HashMap<>();
        if (extVal != null) {
            AuthorityInformationAccess aia = AuthorityInformationAccess.getInstance(X509ExtensionUtil.fromExtensionValue(extVal));
            Arrays.stream(aia.getAccessDescriptions())
                    .forEach(ad -> responseMap.put(ad.getAccessMethod().equals(AccessDescription.id_ad_caIssuers) ? ISSUER_CERTIFICATE : OCSP_URL, ad.getAccessLocation().getName()));
        }
        return responseMap;
    }


    private OCSPReq generateOCSPRequest(X509Certificate issuerCertificate, X509Certificate issuedCertificate) throws Exception {
        X509CertificateHolder issuerHolder = new X509CertificateHolder(issuerCertificate.getEncoded());
        CertificateID certId = new CertificateID(new BcDigestCalculatorProvider().get(CertificateID.HASH_SHA1), issuerHolder, issuedCertificate.getSerialNumber());
        OCSPReqBuilder ocspReqBuilder = new OCSPReqBuilder();
        ocspReqBuilder.addRequest(certId);
        return ocspReqBuilder.build();
    }

    private OCSPResp sendOCSPRequest(OCSPReq ocspReq , String ocspResponderUrl) throws IOException {
        URL responderURL = new URL(ocspResponderUrl);
        HttpURLConnection connection = (HttpURLConnection) responderURL.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/ocsp-request");
        connection.setDoOutput(true);
        connection.getOutputStream().write(ocspReq.getEncoded());
        byte[] responseBytes = connection.getInputStream().readAllBytes();
        return new OCSPResp(responseBytes);
    }

    private boolean checkRevocationStatus(OCSPResp ocspResp) throws OCSPException {
        BasicOCSPResp basicOCSPResp = (BasicOCSPResp) ocspResp.getResponseObject();
        SingleResp[] response = basicOCSPResp.getResponses();
        return Arrays.stream(response)
                .anyMatch(singleResp -> singleResp.getCertStatus() == CertificateStatus.GOOD);
    }

}