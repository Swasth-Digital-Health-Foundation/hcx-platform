package org.swasth.common.utils;

import org.junit.Test;

import java.io.IOException;
import java.security.cert.CertificateException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JWTUtilsTest {

    private final JWTUtils jwtUtils = new JWTUtils();

    @Test
    public void testGetCertificateExpiry() throws CertificateException, IOException {
        assertNotNull(jwtUtils.getCertificateExpiry("https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem"));
    }

    @Test
    public void testIsValidSignature() throws Exception {
        assertTrue(jwtUtils.isValidSignature(getNotificationPayload(), "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem"));
    }

    private String getNotificationPayload(){
        return "eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoidGVzdCJ9.D9LaKmvZfjmNHc3UtrWmILkmGJUdjjKaJtus6H9vRcQZqrd6gTXZ-NQl_oayPc8poFq3vljUAKXPO7PzDNI_N1pd2eqWJ5O-UM1NG_m-v1pKi-kV9HaXucZ0VhAjFS9DEQwZ_CMUMOtgnhh5hKFZZn7ljEbDHaC-2JGDshPNUm_FMWMK447A5B-BJYkEztV47Ony-k4lu9BmfmwsKtqSOO2Y3_qXzuZalShMNt9risNoguY_CQWMFTjV4P_cgsKYDtaRnpgKX96DCO2L2j47BbGx2zHCsMia_LIkxBvEnuEAbDn40zZ17IPOo3BCve8JjmPgCLOYnI3W8HBfHfGegg";
    }
}
