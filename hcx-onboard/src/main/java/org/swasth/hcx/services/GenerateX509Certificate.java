package org.swasth.hcx.services;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

@Service
public class GenerateX509Certificate {

    @Value("${hcxURL}")
    private static  String hcxURL;
    public static X509Certificate generateX509Certificate(PublicKey publicKey, PrivateKey privateKey,String parentParticipantCode) throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException, OperatorCreationException {

        // Prepare the certificate data
        String issuer = String.format("CN=%s",hcxURL);
        String subject = String.format("CN=%s",parentParticipantCode);
        Date startDate = new Date(System.currentTimeMillis());
        Date endDate = new Date(System.currentTimeMillis() + 1095L * 24 * 60 * 60 * 1000); // Valid for 3 year

        // Generate the certificate
        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                new X500Name(issuer), // issuer
                BigInteger.valueOf(System.currentTimeMillis()), // serial number
                startDate, // start date
                endDate, // end date
                new X500Name(subject), // subject
                SubjectPublicKeyInfo.getInstance(publicKey.getEncoded())
        );
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").setProvider("BC").build(privateKey);
        X509CertificateHolder certHolder = certBuilder.build(signer);
        X509Certificate certificate = new JcaX509CertificateConverter().setProvider("BC")
                .getCertificate(certHolder);
        certificate.checkValidity(new Date());
        certificate.verify(publicKey);
        return certificate;
    }

    public String constructKeys(byte[] key,boolean isPublic){
        String prefix;
        String suffix;
        if(isPublic) {
            prefix = "-----BEGIN CERTIFICATE-----";
            suffix = "-----END CERTIFICATE-----";
        }else{
            prefix = "-----BEGIN PRIVATE KEY-----";
            suffix = "-----END PRIVATE KEY-----";
        }
        String LINE_SEPARATOR = " ";
        Base64.Encoder encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());
        String encodedCertText = new String(encoder.encode(key));
        return prefix + LINE_SEPARATOR + encodedCertText + LINE_SEPARATOR + suffix;
    }
}
