package org.swasth.common.validation;

import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.ocsp.*;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.x509.extension.X509ExtensionUtil;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.*;
import java.util.*;

import static org.swasth.common.utils.Constants.ISSUER_CERTIFICATE;
import static org.swasth.common.utils.Constants.OCSP_URL;

public class CertificateRevocation {

    public X509Certificate x509Certificate;

    public CertificateRevocation(X509Certificate x509Certificate) {
        this.x509Certificate = x509Certificate;
    }

    public boolean checkStatus() throws OCSPException, CertificateException, IOException, ClientException, OperatorCreationException, CRLException {
        boolean status = false;
        if (!isCertificateRevokedUsingOCSP(x509Certificate)) {
            if (isCertificateRevokedUsingCRL(x509Certificate)) {
                status = true;
            }
        }
        return status;
    }

    private boolean isCertificateRevokedUsingOCSP(X509Certificate x509Certificate) throws IOException, ClientException, OCSPException, CertificateEncodingException, OperatorCreationException {
        Map<String, Object> certificateAccessInformation = certificateAccessInformation(x509Certificate);
        if (certificateAccessInformation.isEmpty()) {
            throw new ClientException("Certificate revocation details are not available");
        }
        OCSPReq ocspReq = generateOCSPRequest(parseCertificateFromURL(certificateAccessInformation.get(ISSUER_CERTIFICATE).toString()), x509Certificate);
        OCSPResp ocSpResp = sendOCSPRequest(ocspReq, certificateAccessInformation.get(OCSP_URL).toString());
        return checkRevocationStatus(ocSpResp);
    }

    private boolean isCertificateRevokedUsingCRL(X509Certificate x509Certificate) throws IOException, CertificateException, CRLException, ClientException {
        boolean isRevoked = false;
        byte[] crlDistributionPoint = x509Certificate.getExtensionValue(Extension.cRLDistributionPoints.getId());
        if (crlDistributionPoint == null) {
            throw new ClientException("Certificate does not include information about Certificate Revocation Lists (CRLs).");
        }
        CRLDistPoint distPoint = CRLDistPoint.getInstance(JcaX509ExtensionUtils.parseExtensionValue(crlDistributionPoint));
        X509CRLEntry revokedCertificate;
        List<X509CRL> x509CRLList = getDistributedCertificatePoints(distPoint);
        for (X509CRL crl : x509CRLList) {
            revokedCertificate = crl.getRevokedCertificate(x509Certificate.getSerialNumber());
            if (revokedCertificate != null) {
                isRevoked = true;
            }
        }
        return isRevoked;
    }

    private List<X509CRL> getDistributedCertificatePoints(CRLDistPoint distPoint) throws IOException, CertificateException, CRLException {
        List<X509CRL> x509CRLList = new ArrayList<>();
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        for (DistributionPoint dp : distPoint.getDistributionPoints()) {
            for (GeneralName genName : GeneralNames.getInstance(dp.getDistributionPoint().getName()).getNames()) {
                if (genName.getTagNo() == GeneralName.uniformResourceIdentifier) {
                    String url = DERIA5String.getInstance(genName.getName()).getString();
                    X509CRL crl = fetchCRLFromURL(new URL(url), cf);
                    x509CRLList.add(crl);
                }
            }
        }
        return x509CRLList;
    }

    private X509CRL fetchCRLFromURL(URL url, CertificateFactory cf) throws IOException, CRLException {
        try (InputStream inStream = url.openStream()) {
            return (X509CRL) cf.generateCRL(inStream);
        }
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
        byte[] extVal = x509Certificate.getExtensionValue(org.bouncycastle.asn1.x509.Extension.authorityInfoAccess.getId());
        Map<String, Object> responseMap = new HashMap<>();
        if (extVal != null) {
            AuthorityInformationAccess aia = AuthorityInformationAccess.getInstance(X509ExtensionUtil.fromExtensionValue(extVal));
            Arrays.stream(aia.getAccessDescriptions())
                    .forEach(ad -> responseMap.put(ad.getAccessMethod().equals(AccessDescription.id_ad_caIssuers) ? ISSUER_CERTIFICATE : OCSP_URL, ad.getAccessLocation().getName()));
        }
        return responseMap;
    }


    private OCSPReq generateOCSPRequest(X509Certificate issuerCertificate, X509Certificate issuedCertificate) throws OCSPException, CertificateEncodingException, OperatorCreationException, IOException {
        X509CertificateHolder issuerHolder = new X509CertificateHolder(issuerCertificate.getEncoded());
        CertificateID certId = new CertificateID(new BcDigestCalculatorProvider().get(CertificateID.HASH_SHA1), issuerHolder, issuedCertificate.getSerialNumber());
        OCSPReqBuilder ocspReqBuilder = new OCSPReqBuilder();
        ocspReqBuilder.addRequest(certId);
        return ocspReqBuilder.build();
    }

    private OCSPResp sendOCSPRequest(OCSPReq ocspReq, String ocspResponderUrl) throws IOException {
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
        return Arrays.stream(response).anyMatch(singleResp -> singleResp.getCertStatus() == CertificateStatus.GOOD);
    }

}
