package org.swasth.common.validation;

import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Test;
import org.swasth.common.exception.ClientException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CertificateRevocationTest {


    @Test
    public void revocationValidTest() throws CertificateException, OCSPException, IOException, ClientException, OperatorCreationException, CRLException {
        String certificate = "-----BEGIN CERTIFICATE----- MIIE+TCCA+GgAwIBAgISAw88kH5psIajurcRsJmm7uyaMA0GCSqGSIb3DQEBCwUA MDIxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MQswCQYDVQQD EwJSMzAeFw0yMzEyMTkwNTU3MzdaFw0yNDAzMTgwNTU3MzZaMCExHzAdBgNVBAMT FmRldi1vcGQtYXBwLnN3YXN0aC5hcHAwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAw ggEKAoIBAQDfgLcKPnaCC4t+9c50D7HXZajzLaJaSyuZU4kjzz67lClidHQJUuVr ciRx9kJISQoRa4vbaVTNYlYlAdJ6DaZnvRgkMFFnpsxfsdRHfqf6stN26CGdmp4U 6lm+Wfk9oXm8zEowaB1TC3Csg/8NLcjFWAwj9jC18gNn6Gb0SmACvXED2DMbT56H IQaeqfkLGEyDt/PYI7whshlEMIIHykDibk6wGHN3Z754hyJ35L/IQqlVLq7DsL/7 rkIDWPqZN9SRqSVCSgfKzXAa6dC9kKaV0YwguNjdOfF74UrweVvPHbJGDNJHBubC Wc/RGmtv8aDs3HQFoMY/gI61wTvmfuFpAgMBAAGjggIYMIICFDAOBgNVHQ8BAf8E BAMCBaAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMAwGA1UdEwEB/wQC MAAwHQYDVR0OBBYEFBuqvLNXVW9vrOR67GooWaUeGfl5MB8GA1UdIwQYMBaAFBQu sxe3WFbLrlAJQOYfr52LFMLGMFUGCCsGAQUFBwEBBEkwRzAhBggrBgEFBQcwAYYV aHR0cDovL3IzLm8ubGVuY3Iub3JnMCIGCCsGAQUFBzAChhZodHRwOi8vcjMuaS5s ZW5jci5vcmcvMCEGA1UdEQQaMBiCFmRldi1vcGQtYXBwLnN3YXN0aC5hcHAwEwYD VR0gBAwwCjAIBgZngQwBAgEwggEEBgorBgEEAdZ5AgQCBIH1BIHyAPAAdQA7U3d1 Pi25gE6LMFsG/kA7Z9hPw/THvQANLXJv4frUFwAAAYyA3aJpAAAEAwBGMEQCIECh 8R7204MiDVsRl88CVYn9mlLU8aawbOLqRlygE7wHAiAtkqh7lX3FbNgEebisTsgg G3SVCdfPlnfDNPLEt/mDxgB3AEiw42vapkc0D+VqAvqdMOscUgHLVt0sgdm7v6s5 2IRzAAABjIDdomcAAAQDAEgwRgIhALPjekg0gjkQ1iVDE48dGxuwm+T4N4QJlwhk pFvyfyq3AiEAmKkKp39+Dr+rxSV3T0PmnpwWBvIW785gpOzRY/j++MMwDQYJKoZI hvcNAQELBQADggEBABL3ShPW45hft9IpBllVzNXA4AGgdnc8JkCBBTBiqpGoTNk4 92voB+QEu8OYAqgWHSb2Bsac1Sip5gx+YNs5714oTOT4scQ3cDzqk70216PPE1jQ uEpVmthvPOitXYerNA/MatiHdwiaACGDQ7/K12kSVvj82YsJ3wpekKilBIGGqtmj AMMEKexCDlUSsv54+sVZX/PnPwzCu0iHxKgayWqf2DQZ39BCeKae1tnJslzwtRc+ jHv5iCvCNYvHMerGyejd7mKo0XOag02Bo45d/S7KuXcBuYnqIBd2h8iB3z5EoCYe NC3twcXsMvZWYbOiOX13TDuVMcA1aT9WpT0VznU= -----END CERTIFICATE-----";
        X509Certificate x509Certificate = parseCertificateFromString(certificate);
        CertificateRevocation revocation = new CertificateRevocation(x509Certificate);
        assertFalse(revocation.checkRevocationStatus());
    }

    @Test
    public void revokedCertificateTest() throws CertificateException, OCSPException, IOException, ClientException, OperatorCreationException, CRLException {
        String certificate = "-----BEGIN CERTIFICATE----- MIIG4DCCBmagAwIBAgIQBZy2esMzb+7oVrJyhjxvUzAKBggqhkjOPQQDAzBUMQsw CQYDVQQGEwJVUzEXMBUGA1UEChMORGlnaUNlcnQsIEluYy4xLDAqBgNVBAMTI0Rp Z2lDZXJ0IEc1IFRMUyBFQ0MgU0hBMzg0IDIwMjEgQ0ExMB4XDTIzMDMxNjAwMDAw MFoXDTI0MDQxNTIzNTk1OVowge8xEzARBgsrBgEEAYI3PAIBAxMCVVMxFTATBgsr BgEEAYI3PAIBAhMEVXRhaDEdMBsGA1UEDwwUUHJpdmF0ZSBPcmdhbml6YXRpb24x FTATBgNVBAUTDDUyOTk1MzctMDE0MjELMAkGA1UEBhMCVVMxDTALBgNVBAgTBFV0 YWgxDTALBgNVBAcTBExlaGkxFzAVBgNVBAoTDkRpZ2lDZXJ0LCBJbmMuMUcwRQYD VQQDEz5kaWdpY2VydC10bHMtZWNjLXAzODQtcm9vdC1nNS1yZXZva2VkLmNoYWlu LWRlbW9zLmRpZ2ljZXJ0LmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC ggEBAK3SEGd7aOOyi9rknL/GpNSovKvxOeJzrpO7Spq1Ag7KeAtx6kYDm7xgvOXM EPPylKDCvtXl1ic+PYBBVpNZEhHTVefdb9CzsTEcOYLaPFIAOnmie1HHczY57H2f JqvaYqE4VJWAHWuGMf90ZYSkqtoGJJsnLs/Ajd3lawIeUwPCDdWKQiUVG53Ruk5G KRct/Jnxo2qX1GMPt63Q4nvjb0p+4UvWYfBSCAD6UehkdGb1RkbEgKxwBUbFzh7p dQ1WDIVzV0C6OPdt4LUqvVYVw9DpmMSF3YUOvvDEhx1w5bR8JIzmlFYP/IMBx1Bl a9WWbVjCbASq6Z4XrWMpgNoYBEkCAwEAAaOCA7EwggOtMB8GA1UdIwQYMBaAFJtY 3I2mZZjnvAb+GqQVoG/L5qmQMB0GA1UdDgQWBBSsdJukmxK5PpB8IpqyeWO1KJt1 EzBJBgNVHREEQjBAgj5kaWdpY2VydC10bHMtZWNjLXAzODQtcm9vdC1nNS1yZXZv a2VkLmNoYWluLWRlbW9zLmRpZ2ljZXJ0LmNvbTAOBgNVHQ8BAf8EBAMCBaAwHQYD VR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMIGTBgNVHR8EgYswgYgwQqBAoD6G PGh0dHA6Ly9jcmwzLmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydEc1VExTRUNDU0hBMzg0 MjAyMUNBMS0xLmNybDBCoECgPoY8aHR0cDovL2NybDQuZGlnaWNlcnQuY29tL0Rp Z2lDZXJ0RzVUTFNFQ0NTSEEzODQyMDIxQ0ExLTEuY3JsMEoGA1UdIARDMEEwCwYJ YIZIAYb9bAIBMDIGBWeBDAEBMCkwJwYIKwYBBQUHAgEWG2h0dHA6Ly93d3cuZGln aWNlcnQuY29tL0NQUzCBgQYIKwYBBQUHAQEEdTBzMCQGCCsGAQUFBzABhhhodHRw Oi8vb2NzcC5kaWdpY2VydC5jb20wSwYIKwYBBQUHMAKGP2h0dHA6Ly9jYWNlcnRz LmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydEc1VExTRUNDU0hBMzg0MjAyMUNBMS0xLmNy dDAJBgNVHRMEAjAAMIIBfgYKKwYBBAHWeQIEAgSCAW4EggFqAWgAdgDuzdBk1dsa zsVct520zROiModGfLzs3sNRSFlGcR+1mwAAAYbrrv/aAAAEAwBHMEUCIBj/mZ6B QbGrMpNBZoWihQ7+ckmYk1ZbEi/sxPFluT++AiEAzdLuAClGxj5Qw/9yv9XcuVgo LJxuiPNFmI3cifmpY/UAdwA7U3d1Pi25gE6LMFsG/kA7Z9hPw/THvQANLXJv4frU FwAAAYbrrwAfAAAEAwBIMEYCIQCZUf8y3yPDyIA/hfhvZ21ukC3zcdunoqVq+TQW YksMpgIhALfmINPZJmrjy5T/zeHxCxHCHaBpRMIKGAej1JkgeknSAHUAdv+IPwq2 +5VRwmHM9Ye6NLSkzbsp3GhCCp/mZ0xaOnQAAAGG668ATwAABAMARjBEAiBWGuOl X3fLXWfP3ihTE9q/c5sco24KNW0Ij6NaK40hcgIgFKryWdWqqZRoI9LgeBqkzs2p 8ivZEu2wLXX+RoyoCL4wCgYIKoZIzj0EAwMDaAAwZQIxAKcEoh9LUSQ2h/XcESEG LxpGGAcssmrUXBDE0jJPSGgg1ypiE0ay+nYv3TIxVenpIQIweLQmI/ljlQtRBEEh JdnlcMbdN5VOUqtwqd3jEVBgU6vyUmRltnZybBAMUiBpWX+t -----END CERTIFICATE-----";
        X509Certificate x509Certificate = parseCertificateFromString(certificate);
        CertificateRevocation revocation = new CertificateRevocation(x509Certificate);
        assertTrue(revocation.checkRevocationStatus());
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
}
