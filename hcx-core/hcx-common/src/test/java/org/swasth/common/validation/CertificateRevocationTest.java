package org.swasth.common.validation;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CertificateRevocationTest {


//    @Test
//    public void validCertificateTest() throws Exception {
//        String certificate = "-----BEGIN CERTIFICATE----- MIIE8jCCA9qgAwIBAgISBAWf6WTrWjjlUte6qr4mo0t6MA0GCSqGSIb3DQEBCwUA MDIxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MQswCQYDVQQD EwJSMzAeFw0yNDAyMDYxNTAwMjFaFw0yNDA1MDYxNTAwMjBaMB0xGzAZBgNVBAMT EmRldi1oY3guc3dhc3RoLmFwcDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC ggEBANsGx1st9P4hIxlVDlEnJfx0QuzjONf6A1vODXWTT5TcUiIPdbOtC4tRmf77 jv095khldlsPu6aeO6vj/pOetowZoD6xfckNv9YUQqA9viAJv7AYdsdXzr2c1B8S HJWEMOBjj6C94AsPtlXFHsD1RU1PINdNy+vIU+uj6yBOirbwNy2b78Ssju3wTt17 u1mvy2UhKypkIvP18Q+dvqMvL69TeR1OxQeQIclpYpSwnIK7b5eXIJy8iPebh8EL PSMfG25II/ayg4DCMKIWkV6waJgfPQNllpQpRgNgSmjGrbiL+zCunLLGBMxB/AnE HyAWl2KCcpdh7OVXbTkXptcvFF0CAwEAAaOCAhUwggIRMA4GA1UdDwEB/wQEAwIF oDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwDAYDVR0TAQH/BAIwADAd BgNVHQ4EFgQU5xrev4PgHzNXK/pPzI736QjcZvYwHwYDVR0jBBgwFoAUFC6zF7dY VsuuUAlA5h+vnYsUwsYwVQYIKwYBBQUHAQEESTBHMCEGCCsGAQUFBzABhhVodHRw Oi8vcjMuby5sZW5jci5vcmcwIgYIKwYBBQUHMAKGFmh0dHA6Ly9yMy5pLmxlbmNy Lm9yZy8wHQYDVR0RBBYwFIISZGV2LWhjeC5zd2FzdGguYXBwMBMGA1UdIAQMMAow CAYGZ4EMAQIBMIIBBQYKKwYBBAHWeQIEAgSB9gSB8wDxAHYASLDja9qmRzQP5WoC +p0w6xxSActW3SyB2bu/qznYhHMAAAGNfyYhPQAABAMARzBFAiBJYg31O7yv+6Cd tPWz0udvxFtPewD4b8k5y5wo/3dw0wIhAOaIWg0ruqudXOlprBPyiJrHpM3O/bGe 0ZvW7xi44m5YAHcA7s3QZNXbGs7FXLedtM0TojKHRny87N7DUUhZRnEftZsAAAGN fyYhAAAABAMASDBGAiEAhXBdzoACaoL4hZd4vzble7G4MvKEkpz0cVaEBAhznBoC IQCMJ5WL0igVcOtpkmvmDSwAgvgyS7OQ8/vdQqdxLbYbRTANBgkqhkiG9w0BAQsF AAOCAQEAO2NIrK+k7mATP08IcCtqoYOnPzyK0mguUCrJoIFipvKFN36b72oyJsZW yzcbrGJP2Y59Y0DhY28UjnHdxQzbhgI4712uBM7/vq3ruihd7VFV13IqGZqbEKTp EM3ZRgyFo/KpsR0iq3a3M1tgZ3Uxxb7v288xKgprIYaukXQ9MU3DarFsxt/BSgVi KbgwskX76KgR6i/Lr60qeL6CLmWLouMpG198ILzzfNV81KJFPsbaPNRHtOWqY9ud ieM6JURjQD+IOxNcaX0skiQF9AepUvtdZ/vzdhXYqIqX6+6vM2fP6DYcVO7zaoAY CV9sX3TZHS3CYfYPpMaPBUnbpSXtPA== -----END CERTIFICATE-----";
//        X509Certificate x509Certificate = parseCertificateFromString(certificate);
//        CertificateRevocation revocation = new CertificateRevocation(x509Certificate);
//        assertFalse(revocation.checkStatus());
//    }
//
//    @Test
//    public void revokedCertificateTest() throws Exception {
//        String certificate = "-----BEGIN CERTIFICATE----- MIIG4DCCBmagAwIBAgIQBZy2esMzb+7oVrJyhjxvUzAKBggqhkjOPQQDAzBUMQsw CQYDVQQGEwJVUzEXMBUGA1UEChMORGlnaUNlcnQsIEluYy4xLDAqBgNVBAMTI0Rp Z2lDZXJ0IEc1IFRMUyBFQ0MgU0hBMzg0IDIwMjEgQ0ExMB4XDTIzMDMxNjAwMDAw MFoXDTI0MDQxNTIzNTk1OVowge8xEzARBgsrBgEEAYI3PAIBAxMCVVMxFTATBgsr BgEEAYI3PAIBAhMEVXRhaDEdMBsGA1UEDwwUUHJpdmF0ZSBPcmdhbml6YXRpb24x FTATBgNVBAUTDDUyOTk1MzctMDE0MjELMAkGA1UEBhMCVVMxDTALBgNVBAgTBFV0 YWgxDTALBgNVBAcTBExlaGkxFzAVBgNVBAoTDkRpZ2lDZXJ0LCBJbmMuMUcwRQYD VQQDEz5kaWdpY2VydC10bHMtZWNjLXAzODQtcm9vdC1nNS1yZXZva2VkLmNoYWlu LWRlbW9zLmRpZ2ljZXJ0LmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC ggEBAK3SEGd7aOOyi9rknL/GpNSovKvxOeJzrpO7Spq1Ag7KeAtx6kYDm7xgvOXM EPPylKDCvtXl1ic+PYBBVpNZEhHTVefdb9CzsTEcOYLaPFIAOnmie1HHczY57H2f JqvaYqE4VJWAHWuGMf90ZYSkqtoGJJsnLs/Ajd3lawIeUwPCDdWKQiUVG53Ruk5G KRct/Jnxo2qX1GMPt63Q4nvjb0p+4UvWYfBSCAD6UehkdGb1RkbEgKxwBUbFzh7p dQ1WDIVzV0C6OPdt4LUqvVYVw9DpmMSF3YUOvvDEhx1w5bR8JIzmlFYP/IMBx1Bl a9WWbVjCbASq6Z4XrWMpgNoYBEkCAwEAAaOCA7EwggOtMB8GA1UdIwQYMBaAFJtY 3I2mZZjnvAb+GqQVoG/L5qmQMB0GA1UdDgQWBBSsdJukmxK5PpB8IpqyeWO1KJt1 EzBJBgNVHREEQjBAgj5kaWdpY2VydC10bHMtZWNjLXAzODQtcm9vdC1nNS1yZXZv a2VkLmNoYWluLWRlbW9zLmRpZ2ljZXJ0LmNvbTAOBgNVHQ8BAf8EBAMCBaAwHQYD VR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMIGTBgNVHR8EgYswgYgwQqBAoD6G PGh0dHA6Ly9jcmwzLmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydEc1VExTRUNDU0hBMzg0 MjAyMUNBMS0xLmNybDBCoECgPoY8aHR0cDovL2NybDQuZGlnaWNlcnQuY29tL0Rp Z2lDZXJ0RzVUTFNFQ0NTSEEzODQyMDIxQ0ExLTEuY3JsMEoGA1UdIARDMEEwCwYJ YIZIAYb9bAIBMDIGBWeBDAEBMCkwJwYIKwYBBQUHAgEWG2h0dHA6Ly93d3cuZGln aWNlcnQuY29tL0NQUzCBgQYIKwYBBQUHAQEEdTBzMCQGCCsGAQUFBzABhhhodHRw Oi8vb2NzcC5kaWdpY2VydC5jb20wSwYIKwYBBQUHMAKGP2h0dHA6Ly9jYWNlcnRz LmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydEc1VExTRUNDU0hBMzg0MjAyMUNBMS0xLmNy dDAJBgNVHRMEAjAAMIIBfgYKKwYBBAHWeQIEAgSCAW4EggFqAWgAdgDuzdBk1dsa zsVct520zROiModGfLzs3sNRSFlGcR+1mwAAAYbrrv/aAAAEAwBHMEUCIBj/mZ6B QbGrMpNBZoWihQ7+ckmYk1ZbEi/sxPFluT++AiEAzdLuAClGxj5Qw/9yv9XcuVgo LJxuiPNFmI3cifmpY/UAdwA7U3d1Pi25gE6LMFsG/kA7Z9hPw/THvQANLXJv4frU FwAAAYbrrwAfAAAEAwBIMEYCIQCZUf8y3yPDyIA/hfhvZ21ukC3zcdunoqVq+TQW YksMpgIhALfmINPZJmrjy5T/zeHxCxHCHaBpRMIKGAej1JkgeknSAHUAdv+IPwq2 +5VRwmHM9Ye6NLSkzbsp3GhCCp/mZ0xaOnQAAAGG668ATwAABAMARjBEAiBWGuOl X3fLXWfP3ihTE9q/c5sco24KNW0Ij6NaK40hcgIgFKryWdWqqZRoI9LgeBqkzs2p 8ivZEu2wLXX+RoyoCL4wCgYIKoZIzj0EAwMDaAAwZQIxAKcEoh9LUSQ2h/XcESEG LxpGGAcssmrUXBDE0jJPSGgg1ypiE0ay+nYv3TIxVenpIQIweLQmI/ljlQtRBEEh JdnlcMbdN5VOUqtwqd3jEVBgU6vyUmRltnZybBAMUiBpWX+t -----END CERTIFICATE-----";
//        X509Certificate x509Certificate = parseCertificateFromString(certificate);
//        CertificateRevocation revocation = new CertificateRevocation(x509Certificate);
//        assertTrue(revocation.checkStatus());
//    }

    @Test
    public void invalidCertificateTest() throws Exception {
        String certificate = "-----BEGIN CERTIFICATE----- MIIEDzCCAvegAwIBAgIUWbTXVhflZ4OqSgYRHQze59LKav0wDQYJKoZIhvcNAQEL BQAwgZYxCzAJBgNVBAYTAklOMRIwEAYDVQQIDAlLYXJuYXRha2ExEjAQBgNVBAcM CUJlbmdhbHVydTEPMA0GA1UECgwGc3dhc3RoMQwwCgYDVQQLDANIQ1gxDDAKBgNV BAMMA2hjeDEyMDAGCSqGSIb3DQEJARYjT3BlcmF0aW9uc19hZG1pbkBzd2FzdGhh bGxpYW5jZS5vcmcwHhcNMjMwNDI3MTAxOTU4WhcNMjgwNDI1MTAxOTU4WjCBljEL MAkGA1UEBhMCSU4xEjAQBgNVBAgMCUthcm5hdGFrYTESMBAGA1UEBwwJQmVuZ2Fs dXJ1MQ8wDQYDVQQKDAZzd2FzdGgxDDAKBgNVBAsMA0hDWDEMMAoGA1UEAwwDaGN4 MTIwMAYJKoZIhvcNAQkBFiNPcGVyYXRpb25zX2FkbWluQHN3YXN0aGFsbGlhbmNl Lm9yZzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAIrqhMBGwEdYPUzw pN4wOvOQw+SWYuKVSrGpReCMp8AJ0Cxktm0yXEuBa+IVHFGCUCALfeBLk6foMgSm iIroNCfaC6yFJwmXptGUDi8YKZgD5w7Z3w7SumSQnFYarQrksZvu6YGRI07SHIYO CeAe+IfyliSqf0PVfsW4ba33XZfiqD91vkWnc25LxlFYn22Tt+BbMu942LOw9Leb xurGKQn9Ekv2DQQdRG3MlZzaWIFgZO2qE7mh4zaSi+Zo6ftr/8CGdNw5mfb9455a I6Y9HFKOMXK6BEQr5sAZNJ9WrzfTTcIlD6QeD5x86oFsJP7QnQvAtAB4uIqss9LT sdWezdMCAwEAAaNTMFEwHQYDVR0OBBYEFP2lzZCUx2grfZ4V1PfaigEMzykVMB8G A1UdIwQYMBaAFP2lzZCUx2grfZ4V1PfaigEMzykVMA8GA1UdEwEB/wQFMAMBAf8w DQYJKoZIhvcNAQELBQADggEBAFw3pwReHpjlc2/6DG2WsAq0qCfuK8bI3dqqZKZm 5MvWy843Uc1Qg2kWoA0rZ2ZFJPTJ5DbiiDG/TdsIzZV+CX1JAjBtoLRowYXHxaci p+BzuEOugYsI0hUQvWyab8Wtm0vqd5noz+0xF9URCwpUizT+CaP5K0RXojbYnLcB FUjxvZXhZm2K1VF/ndQTXPXBgTEGUQBdFfzgzd2D0DxIqmTT3dAfyweQj9mZGAQ7 xggydF/3f5+MEr239NczBVVHSuZ32ZpVW3y06ptWSu9Wc2sQvU2lM05tHbz0HOSc Xv2SZJJe9fYPoIT8RfQ1/CsFZ09uZL1DMx9s+h2rhtXIRtE= -----END CERTIFICATE-----";
        X509Certificate x509Certificate = parseCertificateFromString(certificate);
        CertificateRevocation revocation = new CertificateRevocation(x509Certificate);
        assertTrue(revocation.checkStatus());
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
