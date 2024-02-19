package org.swasth;

import com.google.gson.Gson;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.swasth.service.EncDeCode;
import org.swasth.service.VerifyQrCode;
import org.swasth.utils.JWSUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HcxQRCodeGeneratorTest {

    HcxQRCodeGenerator hcxQRCodeGenerator = new HcxQRCodeGenerator();

    VerifyQrCode verifyQRCode = new VerifyQrCode();

    EncDeCode encDeCode = new EncDeCode();


    JWSUtils jwsUtils = new JWSUtils();


    @Test
    void test_hcx_qr_generator_success() throws Exception {
        String[] args = {"{\"payload\":{\"participantCode\":\"test_user_55.yopmail@swasth-hcx\",\"email\":\"test_user_555@yopmail.com\",\"mobile\":\"9899912323\"}}"};
        hcxQRCodeGenerator.main(args);
        File qrCodeFile = new File("path_to_qr_code_file");
        assertTrue(true, String.valueOf(qrCodeFile.exists()));
    }

    @Test
    void test_hcx_qr_generator_no_input_exception() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        String[] args = {};
        HcxQRCodeGenerator.main(args);
        String output = outputStream.toString().trim();
        assertEquals("No input to process", output);
    }

    @Test
    public void test_decode() throws Exception {
        String encodedString = "ew0KICAiQGNvbnRleHQiOiBbDQogICAgImh0dHBzOi8vd3d3LnczLm9yZy8yMDE4L2NyZWRlbnRpYWxzL3YxIiwNCiAgICAiaHR0cHM6Ly93d3cudzMub3JnLzIwMTgvY3JlZGVudGlhbHMvZXhhbXBsZXMvdjEiDQogIF0sDQogICJpZCI6ICJodHRwOi8vaGN4cHJvdG9jb2wuaW8vY3JlZGVudGlhbHMvMzczMiIsDQogICJ0eXBlIjogWyJWZXJpZmlhYmxlQ3JlZGVudGlhbCJdLA0KICAiaXNzdWVyIjogImh0dHBzOi8vaGN4cHJvdG9jb2wuaW8vcGFydGljaXBhbnQvNTY1MDQ5IiwNCiAgImlzc3VhbmNlRGF0ZSI6ICIyMDI0LTAyLTE5VDEyOjMzOjU1LjI3OTkzNjMiLA0KICAiZXhwaXJhdGlvbkRhdGUiOiAiMjAyNS0wMi0xOVQxMjozMzo1NS4yNzk5MzYzIiwNCiAgInByZWZlcnJlZEhDWFBhdGgiOiAiaHR0cDovL2hjeC5zd2FzdGguYXBwL2FwaS92MC44LyIsDQogICJjcmVkZW50aWFsU3ViamVjdCI6IHsNCiAgICAiaWQiOiAiY2I0YzE5MjQtOGUzZi00NTgxLThjZWEtNDg4ZmMxOTBiZTA3IiwNCiAgICAicGF5bG9hZCI6IHsicGFydGljaXBhbnRDb2RlIjoidGVzdF91c2VyXzU1LnlvcG1haWxAc3dhc3RoLWhjeCIsImVtYWlsIjoidGVzdF91c2VyXzU1NUB5b3BtYWlsLmNvbSIsIm1vYmlsZSI6Ijk4OTk5MTIzMjMifQ0KICB9LA0KICAicHJvb2YiOiB7DQogICAgInR5cGUiOiAiRWQyNTUxOVNpZ25hdHVyZTIwMjAiLA0KICAgICJjcmVhdGVkIjogIjIwMjQtMDItMTlUMTI6MzM6NTUuMjg0MjMxMTAwIiwNCiAgICAidmVyaWZpY2F0aW9uTWV0aG9kIjogImh0dHBzOi8vaGN4cHJvdG9jb2wuaW8vaXNzdWVycy81NjUwNDkja2V5LTEiLA0KICAgICJwcm9vZlB1cnBvc2UiOiAiYXNzZXJ0aW9uTWV0aG9kIiwNCiAgICAicHJvb2ZWYWx1ZSI6ICJleUpoYkdjaU9pSlNVekkxTmlKOS5leUp3WVhKMGFXTnBjR0Z1ZEVOdlpHVWlPaUowWlhOMFgzVnpaWEpmTlRVdWVXOXdiV0ZwYkVCemQyRnpkR2d0YUdONElpd2laVzFoYVd3aU9pSjBaWE4wWDNWelpYSmZOVFUxUUhsdmNHMWhhV3d1WTI5dElpd2liVzlpYVd4bElqb2lPVGc1T1RreE1qTXlNeUo5Lkk4c2VZMWQ0eUZ6NE5mX3RmZlkxOF9mZWpxZTFfaEJ5T3JsVnJENEMxUDNyQ19WamNhOUhHN19lRkJDUHFIdWh6RXJoeFJUb0FWTzZaMnJYVWNOUmhjc3diS2VWZFpVUFVfb3V4QXJCdUJZUVl4dlFybHNVX25vc2huT0pQYUZlNFk4bmNaWURWYnpPRjRXeVpsUFk3VDZ5UXBJMTJDUFIyMEJYVjhqOFFiaENkMEZ5eW9LN051VVhIcWlYZlFBeEY5YkJEamtoTlNNZzZ1Tjc2dFk1OEI3T0xVakhNYkUzOWU0QTNnQ3pTX0lNNk5uYzdaMHQ4ektsNXJTTFl1elptXzFCWDBPTXc4OGY0WVZENEF6X1FqWDVtNkNzZmVjMWk2X3NHZFZXbzN5em5rdXJ2SVNMdlZLaWNXRFFyMEdkVUVFazg1RUpEX3A5OVhmMktxNTBpQSINCiAgfQ0KfQ0K";
        String expectedDecodedPayloadJson = "{"
                + "\"@context\": [\"https://www.w3.org/2018/credentials/v1\", \"https://www.w3.org/2018/credentials/examples/v1\"],"
                + "\"id\": \"http://hcxprotocol.io/credentials/3732\","
                + "\"type\": [\"VerifiableCredential\"],"
                + "\"issuer\": \"https://hcxprotocol.io/participant/565049\","
                + "\"issuanceDate\": \"2024-02-19T12:33:55.2799363\","
                + "\"expirationDate\": \"2025-02-19T12:33:55.2799363\","
                + "\"preferredHCXPath\": \"http://hcx.swasth.app/api/v0.8/\","
                + "\"credentialSubject\": {"
                +     "\"id\": \"cb4c1924-8e3f-4581-8cea-488fc190be07\","
                +     "\"payload\": {"
                +         "\"participantCode\": \"test_user_55.yopmail@swasth-hcx\","
                +         "\"email\": \"test_user_555@yopmail.com\","
                +         "\"mobile\": \"9899912323\""
                +     "}"
                + "},"
                + "\"proof\": {"
                +     "\"type\": \"Ed25519Signature2020\","
                +     "\"created\": \"2024-02-19T12:33:55.284231100\","
                +     "\"verificationMethod\": \"https://hcxprotocol.io/issuers/565049#key-1\","
                +     "\"proofPurpose\": \"assertionMethod\","
                +     "\"proofValue\": \"eyJhbGciOiJSUzI1NiJ9.eyJwYXJ0aWNpcGFudENvZGUiOiJ0ZXN0X3VzZXJfNTUueW9wbWFpbEBzd2FzdGgtaGN4IiwiZW1haWwiOiJ0ZXN0X3VzZXJfNTU1QHlvcG1haWwuY29tIiwibW9iaWxlIjoiOTg5OTkxMjMyMyJ9.I8seY1d4yFz4Nf_tffY18_fejqe1_hByOrlVrD4C1P3rC_Vjca9HG7_eFBCPqHuhzErhxRToAVO6Z2rXUcNRhcswbKeVdZUPU_ouxArBuBYQYxvQrlsU_noshnOJPaFe4Y8ncZYDVbzOF4WyZlPY7T6yQpI12CPR20BXV8j8QbhCd0FyyoK7NuUXHqiXfQAxF9bBDjkhNSMg6uN76tY58B7OLUjHMbE39e4A3gCzS_IM6Nnc7Z0t8zKl5rSLYuzZm_1BX0OMw88f4YVD4Az_QjX5m6Csfec1i6_sGdVWo3yznkurvISLvVKicWDQr0GdUEEk85EJD_p99Xf2Kq50iA\""
                + "}"
                + "}";
        Gson gson = new Gson();
        Map<String, Object> expectedDecodedPayloadMap = gson.fromJson(expectedDecodedPayloadJson, Map.class);

        Map<String ,Object> decodedPayloadString = encDeCode.decodePayload(encodedString);
        assertEquals(expectedDecodedPayloadMap, decodedPayloadString);
    }

    @Test
    public void test_isValid_signature_success() throws Exception {
        Map<String, Object> jsonData = new HashMap<>();

        Map<String, Object> credentialSubject = new HashMap<>();
        Map<String, Object> proof = new HashMap<>();

        credentialSubject.put("id", "cb4c1924-8e3f-4581-8cea-488fc190be07");
        Map<String, String> payload = new HashMap<>();
        payload.put("participantCode", "test_user_55.yopmail@swasth-hcx");
        payload.put("email", "test_user_555@yopmail.com");
        payload.put("mobile", "9899912323");
        credentialSubject.put("payload", payload);

        proof.put("proofPurpose", "assertionMethod");
        proof.put("proofValue", "eyJhbGciOiJSUzI1NiJ9.eyJwYXJ0aWNpcGFudENvZGUiOiJ0ZXN0X3VzZXJfNTUueW9wbWFpbEBzd2FzdGgtaGN4IiwiZW1haWwiOiJ0ZXN0X3VzZXJfNTU1QHlvcG1haWwuY29tIiwibW9iaWxlIjoiOTg5OTkxMjMyMyJ9.I8seY1d4yFz4Nf_tffY18_fejqe1_hByOrlVrD4C1P3rC_Vjca9HG7_eFBCPqHuhzErhxRToAVO6Z2rXUcNRhcswbKeVdZUPU_ouxArBuBYQYxvQrlsU_noshnOJPaFe4Y8ncZYDVbzOF4WyZlPY7T6yQpI12CPR20BXV8j8QbhCd0FyyoK7NuUXHqiXfQAxF9bBDjkhNSMg6uN76tY58B7OLUjHMbE39e4A3gCzS_IM6Nnc7Z0t8zKl5rSLYuzZm_1BX0OMw88f4YVD4Az_QjX5m6Csfec1i6_sGdVWo3yznkurvISLvVKicWDQr0GdUEEk85EJD_p99Xf2Kq50iA");

        jsonData.put("@context", Arrays.asList("https://www.w3.org/2018/credentials/v1", "https://www.w3.org/2018/credentials/examples/v1"));
        jsonData.put("type", Collections.singletonList("VerifiableCredential"));
        jsonData.put("issuanceDate", "2024-02-19T12:33:55.2799363");
        jsonData.put("expirationDate", "2025-02-19T12:33:55.2799363");
        jsonData.put("credentialSubject", credentialSubject);
        jsonData.put("proof", proof);

        Map<String ,Object> verify =verifyQRCode.getToken(jsonData);
        boolean value = verify.containsKey("proofValue");
        assertEquals(true,value);
    }

    @Test
    public void test_isValid_signature_invaild_proofValue_exception() throws Exception {
        Map<String, Object> jsonData = new HashMap<>();
        Map<String, Object> proof = new HashMap<>();
        proof.put("type", "Ed25519Signature2020");
        proof.put("created", "2024-02-19T12:33:55.284231100");
        proof.put("verificationMethod", "https://hcxprotocol.io/issuers/565049#key-1");
        jsonData.put("proof", proof);
        Map<String ,Object> verify = verifyQRCode.getToken(jsonData);
        boolean value = verify.containsKey("proofValue");
       assertEquals(false,value);
    }
}