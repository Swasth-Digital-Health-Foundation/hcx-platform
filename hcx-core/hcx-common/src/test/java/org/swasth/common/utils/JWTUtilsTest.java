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
        String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALoyryMOM3DpAIGYSSzC1fiuP1LPdOTtxJVCE+HsfI0VPrmSYORh2A+7roadHKi2JZIFZT/F7MPsANBhTRTE++rq5vQObjN3h3b01UdWSdRkjH2nHGYq4t6qAtUu3nabP7PyK7P8xsjHiU5GkVTO/83XqYQVVv8ZAARfNjdzqb8pAgMBAAECgYEAgG39R1zANp1AcCMuNeWd2Q23N9NIea9W7OzK8gZAUr/Yp/9DPcQPV6rI0qkD34rjlziJgddvXCQo25KBrFXCvDVhinHmVxJ9Q6Wg12TjZDLj9mHM3F4tj1JkP9YjftXX0tIJvacd2aVlyOcudfA8muttapEddZnEQN3Nq+vNYgECQQDbl1imX7l9sAO9VyRjsTYWEylED6RwQaUdL0QanC1PNFEutstlaKyg5lPuiU0WTGkhAKSqMjYp3iTzo3BvcPcxAkEA2RHya1Y2a6ABpD8QrfzOS4c1aRRsfeL0dYquCeNAhdbrAoquAZEqSmLVqdWfBv9O1KZwBSNAdHquAK7KCys5eQJAYIrImdTqCz0wV7URNZc6rTfdY6Pw6r2hpxQZwA07yl+49W2+PKZphw/chLnun0gWzECpJH6Q25Vj743Cp+wlcQJBAMtcavZwQG+36ZHMm33E+Cf+NeWKAtI1S7zK/Z25z3sUC/vHnJlPPIWP7og7386YZWwHua62he4Z+OYe2p54ElECQF+al8aE+DvFrRf9atLG0rWswGopGTsq0770Ea1i/xcpfvzxWzrSPjtOn3/pcL6DlMKobhxS4Sjs6Mft4IVkXmY=";
        String sub = "test-verifier-code";
        String iss = "1-d2d56996-1b77-4abb-b9e9-0e6e7343c72e";
        Long expiryTime = Long.valueOf(86400000);
        String token = jwtUtils.generateAuthToken(privateKey,sub,iss,expiryTime);
        assertTrue(jwtUtils.isValidSignature(token,"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-30/hcx-apis/src/test/resources/examples/test-keys/public-key.pem"));
    }
}
