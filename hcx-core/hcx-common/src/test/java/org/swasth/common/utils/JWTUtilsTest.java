package org.swasth.common.utils;

import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    @Test
    public void generateJws() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCG+XLPYiCxrZq71IX+w7uoDGxGI7qy7XaDbL3BJE33ju7rjdrP7wsAOWRvM8BIyWuRZZhl9xG+u7l/7OsZAzGoqI7p+32x+r9IJVzboLDajk6tp/NPg1csc7f2M5Bu6rkLEvrKLz3dgy3Q928rMsD3rSmzBLelfKTo+aDXvCOiw1dMWsZZdkEpCTJxH39Nb2K4S59kO/R2GtSU/QMLq65m34XcMZpDtatA1u1S8JdZNNeMCO+NuFKBzIfvXUCQ8jkf7h612+UP1AYhoyCMFpzUZ9b7liQF9TYpX1Myr/tT75WKuRlkFlcALUrtVskL8KA0w6sA0nX5fORVsuVehVeDAgMBAAECggEAX1n1y5/M7PhxqWO3zYTFGzC7hMlU6XZsFOhLHRjio5KsImgyPlbm9J+W3iA3JLR2c17MTKxAMvg3UbIzW5YwDLAXViC+aW90like8mEQzzVdS7ysXG2ytcqCGUHQNStI0hP0a8T39XbodQl31ZKjU9VW8grRGe12Kse+4ukcW6yRVES+CkyO5BQB+vs3voZavodRGsk/YSt00PtIrFPJgkDuyzzcybKJD9zeJk5W3OGVK1z0on+NXKekRti5FBx/uEkT3+knkz7ZlTDNcyexyeiv7zSL/L6tcszV0Fe0g9vJktqnenEyh4BgbqABPzQR++DaCgW5zsFiQuD0hMadoQKBgQC+rekgpBHsPnbjQ2Ptog9cFzGY6LRGXxVcY7hKBtAZOKAKus5RmMi7Uv7aYJgtX2jt6QJMuE90JLEgdO2vxYG5V7H6Tx+HqH7ftCGZq70A9jFBaba04QAp0r4TnD6v/LM+PGVT8FKtggp+o7gZqXYlSVFm6YzI37G08w43t2j2aQKBgQC1Nluxop8w6pmHxabaFXYomNckziBNMML5GjXW6b0xrzlnZo0p0lTuDtUy2xjaRWRYxb/1lu//LIrWqSGtzu+1mdmV2RbOd26PArKw0pYpXhKFu/W7r6n64/iCisoMJGWSRJVK9X3D4AjPaWOtE+jUTBLOk0lqPJP8K6yiCA6ZCwKBgDLtgDaXm7HdfSN1/Fqbzj5qc3TDsmKZQrtKZw5eg3Y5CYXUHwbsJ7DgmfD5m6uCsCPa+CJFl/MNWcGxeUpZFizKn16bg3BYMIrPMao5lGGNX9p4wbPN5J1HDD1wnc2jULxupSGmLm7pLKRmVeWEvWl4C6XQ+ykrlesef82hzwcBAoGBAKGY3v4y4jlSDCXaqadzWhJr8ffdZUrQwB46NGb5vADxnIRMHHh+G8TLL26RmcET/p93gW518oGg7BLvcpw3nOZaU4HgvQjT0qDvrAApW0V6oZPnAQUlarTU1Uk8kV9wma9tP6E/+K5TPCgSeJPg3FFtoZvcFq0JZoKLRACepL3vAoGAMAUHmNHvDI+v0eyQjQxlmeAscuW0KVAQQR3OdwEwTwdFhp9Il7/mslN1DLBddhj6WtVKLXu85RIGY8I2NhMXLFMgl+q+mvKMFmcTLSJb5bJHyMz/foenGA/3Yl50h9dJRFItApGuEJo/30cG+VmYo2rjtEifktX4mDfbgLsNwsI=";
        Map<String,Object> headers = new HashMap<>();
        Map<String,Object> headersMap = new HashMap<>();
        headersMap.put("alg","RS256");
        headers.put("alg","RS256");
        Map<String,Object> payload = new HashMap<>();
        payload.put("participant_code","testprovider1.apollo@swasth-hcx-dev");
        payload.put("iss", "hcxgateway.swasth@swasth-hcx-dev");
        payload.put("role","admin");
        payload.put("typ","invite");
        payload.put("exp", "1691424319630");
        payload.put("iat", "1691337919630");
        payload.put("jti", "dae0359a-22ac-43e0-b6c3-d9abe4cc5542");
        payload.put("invited_by","mock42@gmail.com");
        payload.put("email","mock-invite@yopmail.com");
        String jwsToken = jwtUtils.generateJWS(headers,payload,privateKey);
        System.out.println(jwsToken);
    }
}
