package org.swasth.common.utils;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@UtilityClass
public class CertificateUtils {

    public static Long getExpiry(String key) throws CertificateException {
        X509Certificate certificate = (X509Certificate) CertificateFactory
                .getInstance("X509")
                .generateCertificate(new ByteArrayInputStream(key.getBytes()));
        return certificate.getNotAfter().toInstant().toEpochMilli();
    }
}
