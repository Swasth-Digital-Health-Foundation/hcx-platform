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
        String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCK6oTARsBHWD1M8KTeMDrzkMPklmLilUqxqUXgjKfACdAsZLZtMlxLgWviFRxRglAgC33gS5On6DIEpoiK6DQn2gushScJl6bRlA4vGCmYA+cO2d8O0rpkkJxWGq0K5LGb7umBkSNO0hyGDgngHviH8pYkqn9D1X7FuG2t912X4qg/db5Fp3NuS8ZRWJ9tk7fgWzLveNizsPS3m8bqxikJ/RJL9g0EHURtzJWc2liBYGTtqhO5oeM2kovmaOn7a//AhnTcOZn2/eOeWiOmPRxSjjFyugREK+bAGTSfVq83003CJQ+kHg+cfOqBbCT+0J0LwLQAeLiKrLPS07HVns3TAgMBAAECggEAFfns32gx90zH1y9YvbJpQ7/6pY5+/Ukl7xOQM5USMrNlZKiCzaVCbErWBjGrGwqft2S/WKehfSt4F5FAQk1izDfWp0fMpFwCmYmFHEEuegHgC1Lp/UksEJ2/WxskQKLA+vdhpyGOsY5UJzGlsU2PqXN1+j34x9fzaBR5ps8JK5kFrbwr8En+hsfShSn3nklMiEyBI7bPDbpaq2Qjpfay/XRzqRdkPhsd+n03ADKGd4axlyAe/VFak6JdpTy+oxWjc2dr2mOWIX9sOCSVOL7sTVX3Wcxhg0FlNbwmbx6nF+UWOJxVO9UNod/OtzSzYsqObJ/z76itUMcBnDl7almtjQKBgQC3Doo8UlvECcQvkrnyNk49oOLaheZA8R2Cw+3oK3ufMvahLEemxCFlZjbkmoeeRySzyPCs7jEr8HK1GQGutv8yS4mkx7lCR2B3jl0NJ7r7kcitZh4VFaMXJORwpMoawMeWvc/u7Xh+OZRMp7c2bfc9114dynCDtwp+dLjG8+y+5wKBgQDCRTlxXu7Kg5AL7H07n5q1r9IfOqfL4inBLXNEciddvaOtFzomjr/6sbGdK+ICeFyk9E12esWnbziaI/E8oSogL4dX+t5hbc5TtvrPs/mILztR1xvUdTNLyT9SOr7dEehytvOLsuHlFcCli7H6nZdrRCVFgeYXWZhMKR5Uagl4NQKBgQCOTHY2+AqvtKvWE2gKmh5uF9/g4Q+hUg2PtkD9JrgdhB9mIKa+Q152lWN8h4d/CWzFeSFmPG7q6ioxDvRY3ZY5gbDI8BzaIeQia/93l3fp0WS/Lk+aMkyqVBpkWiVlcJB2ZKz73Yu6C4Z1pDZu0ELOxtk5rUGTkjlNHez5c2qI5wKBgQC1NbMWQBIHjt6vcKGEGyVJcj5SaPkZodWG3ulVIBH+S6VAEJlqW99RbayaSdOgsDrilwsmh/CUdJdcmEguYLqVmR/q/hOu16kMx3J+iYcp87ymYzsPW19EwuywvCFKvqiPUH4ugeZaNIclJAAZICScps0JX9iLUURRTk1/OgWpPQKBgAD6pmbZCYk0B8TClPvsFSV12HUdMo9f7jteQP4TkmJhi+IOrMmBWVrwH6JgPf/awJPhmGM+g375JswSeugdu7ovcvivsL5JKNMeM35SfrF3xZ3VxX7jcOt/gM1nK9vXgGMJivf5TQIaF0piuwzGb5tlHhnq33FF0Y8MYB2Y4d4B";
        String iss = "1-d2d56996-1b77-4abb-b9e9-0e6e7343c72e";
        Long expiryTime = Long.valueOf(86400000);
        String token = jwtUtils.generateAuthToken(privateKey,iss,expiryTime);
        assertTrue(jwtUtils.isValidSignature(token,"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem"));
    }
}
