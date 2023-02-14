package org.swasth.common.utils;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JWTUtilsTest {

    private final JWTUtils jwtUtils = new JWTUtils();

    @Test
    public void testGetCertificateExpiry() throws Exception {
        assertNotNull(jwtUtils.getCertificateExpiry("https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem"));
    }

    @Test
    public void testIsValidSignature() throws Exception {
        assertTrue(jwtUtils.isValidSignature(getNotificationPayload(), "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem"));
    }

    private String getNotificationPayload(){
        return "eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoidGVzdCJ9.D9LaKmvZfjmNHc3UtrWmILkmGJUdjjKaJtus6H9vRcQZqrd6gTXZ-NQl_oayPc8poFq3vljUAKXPO7PzDNI_N1pd2eqWJ5O-UM1NG_m-v1pKi-kV9HaXucZ0VhAjFS9DEQwZ_CMUMOtgnhh5hKFZZn7ljEbDHaC-2JGDshPNUm_FMWMK447A5B-BJYkEztV47Ony-k4lu9BmfmwsKtqSOO2Y3_qXzuZalShMNt9risNoguY_CQWMFTjV4P_cgsKYDtaRnpgKX96DCO2L2j47BbGx2zHCsMia_LIkxBvEnuEAbDn40zZ17IPOo3BCve8JjmPgCLOYnI3W8HBfHfGegg";
    }

    @Test
    public void generateAuthTokenTest() throws Exception {
        // sample private key
        String privateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDu1pHparsTVsVi97+Qe7mqI3hfDPAFEkTlY5N803JDogckH46UKiEBRYhbSqOeR3lcrT3pCcfMmZq/vA1SEwCDun7XlDo5kb7UztLbuX1ZMC9c5Sg/zOluQlIoRxL9VxljslbmTdxcZ2NkMJ/BAqpegmmvxpoxgJDPiJJXgQ2C+NRAF6oerylp3bsOvJHBxPMI8Te1PxGwONES2KCNYWTC7m5d/gXYdhuNEYU4HtUt2pLMG6F1Z5IksAVvYth0Z2xxmbi88MTmoU/TnrlKAUtTlg0r7BKeNemrl+VKcnIfzKlUBihRnTKcYr70TklWNsP1BIl9Yliphk86gIrNL/bLAgMBAAECggEAC1QRGq2tzuaEOUQACZXcwhWpnXSLI+pwayL0eWy8FDootY1roVp4M0u9gnsBVL4WIw+yio2ktO8qOGLujNCK6zSa1JXaxyVDFGv082gSansPHAxggtowzkKGDBQG8g5CDYkFdyoFRDrBK5zkzu5V72KnKae1Y5iyBa34Jv9fiTE1xupt3FWyYsy0Nbw87IWtUbYxZ45Vd3KDrmEzBq2HUvz2eY2+eR0705LqqzIRoapLvf496uCMl7DikNfD5quJY8sYpBfHg+QDmii2QtfKjvKQ4Me8XDrjb4KbFdnKHcyaioOBpW581UhmIaE60XVMw9ijvpQv6OouT2YIe7FZPQKBgQD4/cYVCjqe8Gts7rGmjb8HmplFqGs1QGvQ5Bi2UBU4ql3KBvkKsOcss3OHiTydpsRCBOSdUcID7qpJDxFuRzBq9EfQpN+NcWNX+txt/JGjIm7vw9LH2f3f7hS+Tpw5lMOqFmKRb9CM0Ql41NiD8Av/nxViZLZoSzx0GXCVdjzGRQKBgQD1j6IFLyy1lmMHDtW0VV5XM5UVm106IiRJSABTAUGhc0FWWTmahdZsAQuLSHSy/93T54pd2uOSOgS5+BylejjFgg52oniarnnsTYMiczEIxkmDKzAiSrmcF9om/MDFKBvek9HG+3uryLG65lE1tzHKNkbP3VcyXgix3OKsRbXhzwKBgEgXOeXXm1dvZrsYdSky+y9ZbK1FcRzu9Qh7Mkh+8VYBxbv7vtmAlL5VisqgSoOeyE4RGpwOEu2OiZEzoqlaRUOCTNS1F+DdBpVvlajmqsgf7RzQhtPEYwQV672aBxOI5gn2Bz3ysOBEmch5X+L7eo3dYpSlckbKGcvHndg4bqo9AoGAUsPznv20ewvSGsnkO6DeaFPJdvvjozlos45iFZgBz/Hx3rG6GZvsloCPrIViWAandMnAuDuTonCNsUSwGxIj2mYoi9HdBn2yOHQzs8PRjhyPIHfJBHXdM4BSlftw2cLYCeAWv5N5rn+5b747Nvik/nvmyB7ZSe+pzJEfkWL5l78CgYAx3R1CXlPUAXrELYwls9CQD8vH9jv5X5W2g9binStbJCAw2luOhbYh54Y+cB2PAsl+cR5VpHxXkgyUhHKLqjo+ZnHQAGABAazTjdsVKB1s2hZKrr0Rs8yZSW0NFGaYEwG1Y5JjYdLaxSmVQd09gnJZQciwHSBNQNWIqpTfQEhNZw==";
        String sub = "test-verifier-code";
        String iss = "1-d2d56996-1b77-4abb-b9e9-0e6e7343c72e";
        Long expiryTime = Long.valueOf(86400000);
        String token = jwtUtils.generateAuthToken(privateKey,sub,iss,expiryTime);
        assertTrue(jwtUtils.isValidSignature(token,"https://raw.githubusercontent.com/AbhiGaddi/demo-SpringBootAp/master/src/main/resources/publickey.pem"));
    }
}
