package org.swasth.hcx.controllers.v1;

import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseSpec;
import org.swasth.hcx.services.OnboardService;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.swasth.common.utils.Constants.SUCCESSFUL;

class OnboardControllerTests extends BaseSpec{
    @Autowired
    private OnboardController onboardController;
    @Mock
    private OnboardService onboardService;

    @Test
    void testVerify_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                                "    \"timestamp\": 1697191734176,\n" +
                                "    \"participants\": [\n" +
                                "        {\n" +
                                "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/wemeanhospital%2Bmock_payor.yopmail%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                                "            \"participant_name\": \"wemeanhospital Mock Payor\",\n" +
                                "            \"endpoint_url\": \"http://a6a5d9138995a45b2bf9fd3f72b84367-915129339.ap-south-1.elb.amazonaws.com:8080/v0.7\",\n" +
                                "            \"roles\": [\n" +
                                "                \"provider\"\n" +
                                "            ],\n" +
                                "            \"scheme_code\": \"default\",\n" +
                                "            \"primary_email\": \"wemeanhospital+mock_payor@yopmail.com\",\n" +
                                "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/wemeanhospital%2Bmock_payor.yopmail%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                                "            \"status\": \"Active\",\n" +
                                "            \"participant_code\": \"wemeanhospital+mock_payor.yopmail@swasth-hcx-dev\",\n" +
                                "            \"sigining_cert_expiry\": 1779007885000,\n" +
                                "            \"encryption_cert_expiry\": 1779007885000,\n" +
                                "            \"osOwner\": [\n" +
                                "                \"62c12021-eb1a-49ff-9496-d7a65a616930\"\n" +
                                "            ],\n" +
                                "            \"osCreatedAt\": \"2023-05-18T08:51:30.271Z\",\n" +
                                "            \"osUpdatedAt\": \"2023-09-14T05:34:07.932Z\",\n" +
                                "            \"osid\": \"93dac853-9089-4df6-9cbe-6b4b9acdc27e\",\n" +
                                "            \"@type\": \"Organisation\",\n" +
                                "            \"primary_mobile\": \"\"\n" +
                                "        }\n" +
                                "    ]\n" +
                                "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"participant_code\": \"provider.clinic_test_u_312076@swasth-hcx-dev\"}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                        .setBody("{\n" +
                                "    \"timestamp\": 1697200288894,\n" +
                                "    \"users\": []\n" +
                                "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697200288894,\n" +
                        "    \"users\": []\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"user_id\": \"obama02@yopmail.com\"}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"result\": [\n" +
                        "        {\n" +
                        "            \"user_id\": \"obama02@yopmail.com\",\n" +
                        "            \"status\": \"successful\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"overallStatus\": \"successful\",\n" +
                        "    \"timestamp\": 1697199663810\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying,    createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        String requestBody = verifyRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY).content(requestBody).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void testVerify_user_exists_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697191734176,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/wemeanhospital%2Bmock_payor.yopmail%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"participant_name\": \"wemeanhospital Mock Payor\",\n" +
                        "            \"endpoint_url\": \"http://a6a5d9138995a45b2bf9fd3f72b84367-915129339.ap-south-1.elb.amazonaws.com:8080/v0.7\",\n" +
                        "            \"roles\": [\n" +
                        "                \"provider\"\n" +
                        "            ],\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"primary_email\": \"wemeanhospital+mock_payor@yopmail.com\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/wemeanhospital%2Bmock_payor.yopmail%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_code\": \"wemeanhospital+mock_payor.yopmail@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1779007885000,\n" +
                        "            \"encryption_cert_expiry\": 1779007885000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"62c12021-eb1a-49ff-9496-d7a65a616930\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-05-18T08:51:30.271Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-09-14T05:34:07.932Z\",\n" +
                        "            \"osid\": \"93dac853-9089-4df6-9cbe-6b4b9acdc27e\",\n" +
                        "            \"@type\": \"Organisation\",\n" +
                        "            \"primary_mobile\": \"\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"participant_code\": \"provider.clinic_test_u_312076@swasth-hcx-dev\"}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698324213918,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"obama02@yopmail.com\",\n" +
                        "            \"mobile\": \"9620499129\",\n" +
                        "            \"user_name\": \"test_user_12 Admin\",\n" +
                        "            \"user_id\": \"obama02@yopmail.com\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-10-13T12:21:03.685Z\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"participant_code\": \"provider.clinic_test_u_312076@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"ba43e7ba-3429-44d8-b83d-f4be25de9756\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-10-13T12:21:03.685Z\",\n" +
                        "                    \"role\": \"config-manager\",\n" +
                        "                    \"participant_code\": \"provider.clinic_test_u_312076@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"4c09171a-fdd3-4dea-b048-5db6a33e8646\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-10-13T12:21:03.685Z\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"participant_code\": \"wemeanhospital+mock_payor.yopmail@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"f71229ba-785a-487d-97a6-c572210364cb\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"created_by\": \"clin_test_u_970607@swasth-hcx\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"739872d1-b9bc-43df-a9c9-3689bac1840e\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-10-12T13:37:13.435Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-13T12:21:03.928Z\",\n" +
                        "            \"osid\": \"ca5c442d-bb47-4748-aa84-fe48a3a024aa\",\n" +
                        "            \"@type\": \"User\",\n" +
                        "            \"tenant\": [\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-10-13T12:21:03.928Z\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"participant_code\": \"wemeanhospital+mock_payor.yopmail@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"f71229ba-785a-487d-97a6-c572210364cb\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-10-13T12:21:03.928Z\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"participant_code\": \"provider.clinic_test_u_312076@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"ba43e7ba-3429-44d8-b83d-f4be25de9756\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-10-13T12:21:03.928Z\",\n" +
                        "                    \"role\": \"config-manager\",\n" +
                        "                    \"participant_code\": \"provider.clinic_test_u_312076@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"4c09171a-fdd3-4dea-b048-5db6a33e8646\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698324213918,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"obama02@yopmail.com\",\n" +
                        "            \"mobile\": \"9620499129\",\n" +
                        "            \"user_name\": \"test_user_12 Admin\",\n" +
                        "            \"user_id\": \"obama02@yopmail.com\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-10-13T12:21:03.685Z\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"participant_code\": \"provider.clinic_test_u_312076@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"ba43e7ba-3429-44d8-b83d-f4be25de9756\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-10-13T12:21:03.685Z\",\n" +
                        "                    \"role\": \"config-manager\",\n" +
                        "                    \"participant_code\": \"provider.clinic_test_u_312076@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"4c09171a-fdd3-4dea-b048-5db6a33e8646\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-10-13T12:21:03.685Z\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"participant_code\": \"wemeanhospital+mock_payor.yopmail@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"f71229ba-785a-487d-97a6-c572210364cb\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"created_by\": \"clin_test_u_970607@swasth-hcx\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"739872d1-b9bc-43df-a9c9-3689bac1840e\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-10-12T13:37:13.435Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-13T12:21:03.928Z\",\n" +
                        "            \"osid\": \"ca5c442d-bb47-4748-aa84-fe48a3a024aa\",\n" +
                        "            \"@type\": \"User\",\n" +
                        "            \"tenant\": [\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-10-13T12:21:03.928Z\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"participant_code\": \"wemeanhospital+mock_payor.yopmail@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"f71229ba-785a-487d-97a6-c572210364cb\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-10-13T12:21:03.928Z\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"participant_code\": \"provider.clinic_test_u_312076@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"ba43e7ba-3429-44d8-b83d-f4be25de9756\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-10-13T12:21:03.928Z\",\n" +
                        "                    \"role\": \"config-manager\",\n" +
                        "                    \"participant_code\": \"provider.clinic_test_u_312076@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"4c09171a-fdd3-4dea-b048-5db6a33e8646\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"result\": [\n" +
                        "        {\n" +
                        "            \"user_id\": \"obama02@yopmail.com\",\n" +
                        "            \"status\": \"successful\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"overallStatus\": \"successful\",\n" +
                        "    \"timestamp\": 1697199663810\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying,    createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        String requestBody = verifyRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY).content(requestBody).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }
    @Test
    void testVerifyPayor_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697191734176,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/wemeanhospital%2Bmock_payor.yopmail%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"participant_name\": \"wemeanhospital Mock Payor\",\n" +
                        "            \"endpoint_url\": \"http://a6a5d9138995a45b2bf9fd3f72b84367-915129339.ap-south-1.elb.amazonaws.com:8080/v0.7\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"primary_email\": \"wemeanhospital+mock_payor@yopmail.com\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/wemeanhospital%2Bmock_payor.yopmail%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_code\": \"wemeanhospital+mock_payor.yopmail@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1779007885000,\n" +
                        "            \"encryption_cert_expiry\": 1779007885000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"62c12021-eb1a-49ff-9496-d7a65a616930\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-05-18T08:51:30.271Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-09-14T05:34:07.932Z\",\n" +
                        "            \"osid\": \"93dac853-9089-4df6-9cbe-6b4b9acdc27e\",\n" +
                        "            \"@type\": \"Organisation\",\n" +
                        "            \"primary_mobile\": \"\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"participant_code\": \"provider.clinic_test_u_312076@swasth-hcx-dev\"}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697200288894,\n" +
                        "    \"users\": []\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697200288894,\n" +
                        "    \"users\": []\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"user_id\": \"obama02@yopmail.com\"}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"result\": [\n" +
                        "        {\n" +
                        "            \"user_id\": \"obama02@yopmail.com\",\n" +
                        "            \"status\": \"successful\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"overallStatus\": \"successful\",\n" +
                        "    \"timestamp\": 1697199663810\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying,    createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        String requestBody = verifyPayorRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY).content(requestBody).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_sendVerification_link_moreThan1count_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698217240571,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user_52@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"Abhishek\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"test_user_52.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"15765255-aea0-4151-ab47-d575de172857\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osid\": \"89e2b4ab-490f-4eb5-9fb2-1222ceb7c42c\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " +
                " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',false,false,'successful',3,'2023-10-26T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        String requestBodyJson = verificationLinkRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFICATION_LINK_SEND).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_verify_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"participant_code\": \"provider.clinic_test_u_312076@swasth-hcx-dev\"}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697200288894,\n" +
                        "    \"users\": []\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697200288894,\n" +
                        "    \"users\": []\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"user_id\": \"obama02@yopmail.com\"}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"result\": [\n" +
                        "        {\n" +
                        "            \"user_id\": \"obama02@yopmail.com\",\n" +
                        "            \"status\": \"successful\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"overallStatus\": \"successful\",\n" +
                        "    \"timestamp\": 1697199663810\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying,    createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        String requestBody = verifyRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY).content(requestBody).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(500, status);
    }
    @Test
    void test_onboard_update_withEnv_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697523436655,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user-hcx-5@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test_user-hcx-5\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"participant_code\": \"test_user-hcx-5.yopmail@swasth-hcx\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"9306930e-ac74-4d52-9f75-bfd5ca4db867\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"3cb8233d-01d9-4e29-af0c-d00c2f28d353\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697524745246,\n" +
                        "    \"result\": {\n" +
                        "        \"identity_verification\": \"rejected\",\n" +
                        "        \"email_verified\": true,\n" +
                        "        \"phone_verified\": true,\n" +
                        "        \"participant_code\": \"test_user_54.yopmail@swasth-hcx\",\n" +
                        "        \"communication_verification\": \"successful\"\n" +
                        "    },\n" +
                        "    \"status\": \"SUCCESSFUL\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697523436655,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user-hcx-5@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test_user-hcx-5\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"participant_code\": \"test_user-hcx-5.yopmail@swasth-hcx\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"9306930e-ac74-4d52-9f75-bfd5ca4db867\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"3cb8233d-01d9-4e29-af0c-d00c2f28d353\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698210049496,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"test-1@gmail.com\",\n" +
                        "            \"user_name\": \"test-user-92\",\n" +
                        "            \"created_by\": \"test-user-92\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"participant_code\": \"test-participant-code-4@swasth\",\n" +
                        "                    \"roles\": [\n" +
                        "                        \"payor\"\n" +
                        "                    ],\n" +
                        "                    \"osCreatedAt\": \"2023-05-23T07:26:03.791Z\",\n" +
                        "                    \"osUpdatedAt\": \"2023-05-23T07:26:03.791Z\",\n" +
                        "                    \"osid\": \"1ebaa072-b6f5-4cd9-9534-d55a52aaa6af\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"user_id\": \"test-1.gmail@swasth-hcx\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"0d7d0600-bc46-4310-9c2c-71d2aac3078a\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-05-23T07:26:03.791Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-05-23T07:26:03.791Z\",\n" +
                        "            \"osid\": \"2b410f9e-a96a-4494-9f87-64016982ee3c\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        Mockito.doNothing().when(kafkaClient).send(anyString(),anyString(),anyString());
        Mockito.when(mockEventGenerator.getEmailMessageEvent(anyString(),anyString(),anyList(),anyList(),anyList())).thenReturn("mocked-event");
        Mockito.when(freemarkerService.renderTemplate(any(),anyMap())).thenReturn("freemarker");
        Response resp = new Response();
        resp.setStatus(SUCCESSFUL);
        Mockito.when(onboardService.generateAndSetPassword(any(),anyString())).thenReturn(resp);
        Mockito.doNothing().when(onboardService).setKeycloakPassword(anyString(),anyMap());
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS mock_participant");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('test_user_54.yopmail@swasth-hcx','test_user_54@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('test_user_54@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','test_user_54.yopmail@swasth-hcx')");
        postgreSQLClient.execute("CREATE TABLE  mock_participant(parent_participant_code character varying,  child_participant_code character varying NOT NULL PRIMARY KEY ,primary_email character varying, password character varying,private_key character varying)");
        postgreSQLClient.execute("INSERT INTO mock_participant(parent_participant_code,child_participant_code,primary_email,password,private_key)"+"VALUES ('test_user_54.yopmail@swasth-hcx','testprovider1.apollo@swasth-hcx-dev','test_user_54@yopmail.com','Fgkt#54kfgkN','MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDX8KLtg3R9jiqy0GHNdPgo7Kb+eOQlBeWFHAKXY/1TG3m6CaVwtQzof3akE457WFtGR1bP41/0AzDIvmKTVXQCwer9wB2GYuQfrUjvPbFgECcBOC3Dv0Ak/3+T05ntnVrwxYLowEQl53IQt7IWOUCLIBNYjn3B6Wx99PPY4UcHSL6HoWN4/KnAKJiHcUtMSuIUfAG4BMtWDt1yKTRBPNwZpwhka0NTTnPnfVP5z3amtC1o6jWsNNntXPqyR5CNQn8BLbaMtnzQG4K8wfY7rPIgr7mYCteIoEpEMkPu4viBAGUlO047ybugbSRSP28D79AP9QlgI3Qn4bB9wmzTi9UtAgMBAAECggEBAJfrYQTOdfcbPOj+d8BPKYPJMpdXP9LYOiiSkzQlEYUVkGcVAEKx7XnoqvQ2GginGdfwup+ZLNmEIR8p6joTZYHHIecR8POpwSqUA/rkoVSfKIHQH0pW0+7znbLHrMSh7ufzXO0YzxkHopUmV3ERKFp434NvBASXj09yNNgBbbIt7WKTqjuNA/lOY+VfhkSxoxVMnRJYtq3vbm8x/+QU1qthUpt6UbbGHTOHMl2MsS/MRPLZUbG00E/nhyH5/w+ZOApHCQc2ZTuXCqhRqTDkeWvgdnfX/pxsVvhBAq5MltlyhBiKUGB90PRISJ/Wu6462ywiiK9zUozCyDXtWQJ8QkECgYEA9FhyEY8Sp9tKPQECIRCFXIYpv8kAuTbw+1sgi10SBcDPSLY1HEO7RZu94+G3CygnD3bEaCzaWLQ0FlbPB4wKl1bBoAY735LoAcZR0nc8zTbm4nLl0ibu2NUzRfpW6X1dgFmuHUpcbAr2SWLoec1y5OvwScplOcBQXK7r6vPfPdECgYEA4j1ZNpFDwHxxxSAs1tNZlhAz1DFPw+LRb+Ap34UaMWA18ZGtvKwzZfFrtbGSYhu05SO+sYZbk4Z1s84gCtj8HqiUFFvI8v0ozwPRPEzFaipGcVNFRCornoxcv6EC3x1CFhlowHt2MphADfRQA7zlJk/HIq65DD9weXzMr4aTLJ0CgYEAkueiHSBxzO2w4qB6kTqHk6st6pqEjtaTZ+vP0zovnbngZgz2PXoTW7RZJGsOS+zmHwv+5cshs3cUYeHrMtRlgbutSfK1iKOgTYDYrLr3mUHK6pa9ye2SaFc2LnpmSpcO4h4I6p9MlcC5dkG7F5AH5c5cd2DyHxiauD6KpIXe0CECgYAxCXUV08SoqxCJ1qCBa8wGL7rcKlgMsFQO+Lp6vUHhI+ZtVtMeiwCU/xAGkNeWtkSuSeIiXmno/wLyFyJw13lGN+now8A5k')");
        String requestBodyJson = updateRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_ONBOARD_UPDATE).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_onboard_update_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697523436655,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user-hcx-5@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test_user-hcx-5\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"participant_code\": \"test_user-hcx-5.yopmail@swasth-hcx\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"9306930e-ac74-4d52-9f75-bfd5ca4db867\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"3cb8233d-01d9-4e29-af0c-d00c2f28d353\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697524745246,\n" +
                        "    \"result\": {\n" +
                        "        \"identity_verification\": \"rejected\",\n" +
                        "        \"email_verified\": true,\n" +
                        "        \"phone_verified\": true,\n" +
                        "        \"participant_code\": \"test_user_54.yopmail@swasth-hcx\",\n" +
                        "        \"communication_verification\": \"successful\"\n" +
                        "    },\n" +
                        "    \"status\": \"SUCCESSFUL\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697523436655,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user-hcx-5@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test_user-hcx-5\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"participant_code\": \"test_user-hcx-5.yopmail@swasth-hcx\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"9306930e-ac74-4d52-9f75-bfd5ca4db867\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"3cb8233d-01d9-4e29-af0c-d00c2f28d353\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698210049496,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"test-1@gmail.com\",\n" +
                        "            \"user_name\": \"test-user-92\",\n" +
                        "            \"created_by\": \"test-user-92\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"participant_code\": \"test-participant-code-4@swasth\",\n" +
                        "                    \"roles\": [\n" +
                        "                        \"payor\"\n" +
                        "                    ],\n" +
                        "                    \"osCreatedAt\": \"2023-05-23T07:26:03.791Z\",\n" +
                        "                    \"osUpdatedAt\": \"2023-05-23T07:26:03.791Z\",\n" +
                        "                    \"osid\": \"1ebaa072-b6f5-4cd9-9534-d55a52aaa6af\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"user_id\": \"test-1.gmail@swasth-hcx\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"0d7d0600-bc46-4310-9c2c-71d2aac3078a\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-05-23T07:26:03.791Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-05-23T07:26:03.791Z\",\n" +
                        "            \"osid\": \"2b410f9e-a96a-4494-9f87-64016982ee3c\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        Mockito.doNothing().when(kafkaClient).send(anyString(),anyString(),anyString());
        Mockito.when(mockEventGenerator.getEmailMessageEvent(anyString(),anyString(),anyList(),anyList(),anyList())).thenReturn("mocked-event");
        Mockito.when(freemarkerService.renderTemplate(any(),anyMap())).thenReturn("freemarker");
        Response resp = new Response();
        resp.setStatus(SUCCESSFUL);
        Mockito.when(onboardService.generateAndSetPassword(any(),anyString())).thenReturn(resp);
        Mockito.doNothing().when(onboardService).setKeycloakPassword(anyString(),anyMap());
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS mock_participant");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('test_user_54.yopmail@swasth-hcx','test_user_54@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('test_user_54@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','test_user_54.yopmail@swasth-hcx')");
        postgreSQLClient.execute("CREATE TABLE  mock_participant(parent_participant_code character varying,  child_participant_code character varying NOT NULL PRIMARY KEY ,primary_email character varying, password character varying,private_key character varying)");
        postgreSQLClient.execute("INSERT INTO mock_participant(parent_participant_code,child_participant_code,primary_email,password,private_key)"+"VALUES ('test_user_54.yopmail@swasth-hcx','testprovider1.apollo@swasth-hcx-dev','test_user_54@yopmail.com','Fgkt#54kfgkN','MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDX8KLtg3R9jiqy0GHNdPgo7Kb+eOQlBeWFHAKXY/1TG3m6CaVwtQzof3akE457WFtGR1bP41/0AzDIvmKTVXQCwer9wB2GYuQfrUjvPbFgECcBOC3Dv0Ak/3+T05ntnVrwxYLowEQl53IQt7IWOUCLIBNYjn3B6Wx99PPY4UcHSL6HoWN4/KnAKJiHcUtMSuIUfAG4BMtWDt1yKTRBPNwZpwhka0NTTnPnfVP5z3amtC1o6jWsNNntXPqyR5CNQn8BLbaMtnzQG4K8wfY7rPIgr7mYCteIoEpEMkPu4viBAGUlO047ybugbSRSP28D79AP9QlgI3Qn4bB9wmzTi9UtAgMBAAECggEBAJfrYQTOdfcbPOj+d8BPKYPJMpdXP9LYOiiSkzQlEYUVkGcVAEKx7XnoqvQ2GginGdfwup+ZLNmEIR8p6joTZYHHIecR8POpwSqUA/rkoVSfKIHQH0pW0+7znbLHrMSh7ufzXO0YzxkHopUmV3ERKFp434NvBASXj09yNNgBbbIt7WKTqjuNA/lOY+VfhkSxoxVMnRJYtq3vbm8x/+QU1qthUpt6UbbGHTOHMl2MsS/MRPLZUbG00E/nhyH5/w+ZOApHCQc2ZTuXCqhRqTDkeWvgdnfX/pxsVvhBAq5MltlyhBiKUGB90PRISJ/Wu6462ywiiK9zUozCyDXtWQJ8QkECgYEA9FhyEY8Sp9tKPQECIRCFXIYpv8kAuTbw+1sgi10SBcDPSLY1HEO7RZu94+G3CygnD3bEaCzaWLQ0FlbPB4wKl1bBoAY735LoAcZR0nc8zTbm4nLl0ibu2NUzRfpW6X1dgFmuHUpcbAr2SWLoec1y5OvwScplOcBQXK7r6vPfPdECgYEA4j1ZNpFDwHxxxSAs1tNZlhAz1DFPw+LRb+Ap34UaMWA18ZGtvKwzZfFrtbGSYhu05SO+sYZbk4Z1s84gCtj8HqiUFFvI8v0ozwPRPEzFaipGcVNFRCornoxcv6EC3x1CFhlowHt2MphADfRQA7zlJk/HIq65DD9weXzMr4aTLJ0CgYEAkueiHSBxzO2w4qB6kTqHk6st6pqEjtaTZ+vP0zovnbngZgz2PXoTW7RZJGsOS+zmHwv+5cshs3cUYeHrMtRlgbutSfK1iKOgTYDYrLr3mUHK6pa9ye2SaFc2LnpmSpcO4h4I6p9MlcC5dkG7F5AH5c5cd2DyHxiauD6KpIXe0CECgYAxCXUV08SoqxCJ1qCBa8wGL7rcKlgMsFQO+Lp6vUHhI+ZtVtMeiwCU/xAGkNeWtkSuSeIiXmno/wLyFyJw13lGN+now8A5k')");
        String requestBodyJson = onboardUpdateRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_ONBOARD_UPDATE).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_onboard_update_withEnv_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697523436655,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user-hcx-5@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test_user-hcx-5\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"participant_code\": \"test_user-hcx-5.yopmail@swasth-hcx\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"9306930e-ac74-4d52-9f75-bfd5ca4db867\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"3cb8233d-01d9-4e29-af0c-d00c2f28d353\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697524745246,\n" +
                        "    \"result\": {\n" +
                        "        \"identity_verification\": \"rejected\",\n" +
                        "        \"email_verified\": true,\n" +
                        "        \"phone_verified\": true,\n" +
                        "        \"participant_code\": \"test_user_54.yopmail@swasth-hcx\",\n" +
                        "        \"communication_verification\": \"successful\"\n" +
                        "    },\n" +
                        "    \"status\": \"SUCCESSFUL\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        Mockito.doNothing().when(kafkaClient).send(anyString(),anyString(),anyString());
        Mockito.when(mockEventGenerator.getEmailMessageEvent(anyString(),anyString(),anyList(),anyList(),anyList())).thenReturn("mocked-event");
        Mockito.when(freemarkerService.renderTemplate(any(),anyMap())).thenReturn("freemarker");
        Response resp = new Response();
        resp.setStatus(SUCCESSFUL);
        Mockito.when(onboardService.generateAndSetPassword(any(),anyString())).thenReturn(resp);
        Mockito.doNothing().when(onboardService).setKeycloakPassword(anyString(),anyMap());
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS mock_participant");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('test_user_54.yopmail@swasth-hcx','test_user_54@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('test_user_54@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','test_user_54.yopmail@swasth-hcx')");
        postgreSQLClient.execute("CREATE TABLE  mock_participant(parent_participant_code character varying,  child_participant_code character varying NOT NULL PRIMARY KEY ,primary_email character varying, password character varying,private_key character varying)");
        String requestBodyJson = updateRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_ONBOARD_UPDATE).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(500, status);
    }

    @Test
     void test_onboard_update_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697524745246,\n" +
                        "    \"result\": {\n" +
                        "        \"identity_verification\": \"rejected\",\n" +
                        "        \"email_verified\": true,\n" +
                        "        \"phone_verified\": true,\n" +
                        "        \"participant_code\": \"test_user_54.yopmail@swasth-hcx\",\n" +
                        "        \"communication_verification\": \"successful\"\n" +
                        "    },\n" +
                        "    \"status\": \"SUCCESSFUL\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("")
                .addHeader("Content-Type", "application/json"));
        Mockito.doNothing().when(kafkaClient).send(anyString(),anyString(),anyString());
        Mockito.when(mockEventGenerator.getEmailMessageEvent(anyString(),anyString(),anyList(),anyList(),anyList())).thenReturn("mocked-event");
        Mockito.when(freemarkerService.renderTemplate(any(),anyMap())).thenReturn("freemarker");
        Response resp = new Response();
        resp.setStatus(SUCCESSFUL);
        Mockito.when(onboardService.generateAndSetPassword(any(),anyString())).thenReturn(resp);
        Mockito.doNothing().when(onboardService).setKeycloakPassword(anyString(),anyMap());
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " +
                " VALUES('test_user_54.yopmail@swasth-hcx','test_user_54@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('test_user_54@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','test_user_54.yopmail@swasth-hcx')");
        String requestBodyJson = updateRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_ONBOARD_UPDATE).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(500, status);
    }

    @Test
    void test_verify_identity_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698211735440,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user_52@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"Abhishek\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"test_user_52.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"15765255-aea0-4151-ab47-d575de172857\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osid\": \"89e2b4ab-490f-4eb5-9fb2-1222ceb7c42c\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"participant_code\": \"test_user_52.yopmail@swasth-hcx\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698211735440,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user_52@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"Abhishek\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"test_user_52.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"15765255-aea0-4151-ab47-d575de172857\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osid\": \"89e2b4ab-490f-4eb5-9fb2-1222ceb7c42c\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698213049238,\n" +
                        "    \"users\": []\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('test_user_52@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','test_user_52.yopmail@swasth-hcx')");
        String requestBodyJson = verifyIdentityRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY_IDENTITY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_verify_identity_reject_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698211735440,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user_52@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"Abhishek\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"test_user_52.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"15765255-aea0-4151-ab47-d575de172857\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osid\": \"89e2b4ab-490f-4eb5-9fb2-1222ceb7c42c\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"participant_code\": \"test_user_52.yopmail@swasth-hcx\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('test_user_52@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','test_user_52.yopmail@swasth-hcx')");
        String requestBodyJson = verifyIdentityRejectRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY_IDENTITY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    void test_verify_identity_invalid_status_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698211735440,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user_52@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"Abhishek\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"test_user_52.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"15765255-aea0-4151-ab47-d575de172857\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osid\": \"89e2b4ab-490f-4eb5-9fb2-1222ceb7c42c\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"participant_code\": \"test_user_52.yopmail@swasth-hcx\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('test_user_52@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','test_user_52.yopmail@swasth-hcx')");
        String requestBodyJson = verifyIdentityOtherThanAllowedStatus();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY_IDENTITY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    void test_verify_identity_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"participant_code\": \"test_user_52.yopmail@swasth-hcx\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698213049238,\n" +
                        "    \"users\": []\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('test_user_52@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','test_user_52.yopmail@swasth-hcx')");
        String requestBodyJson = verifyIdentityRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY_IDENTITY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(500, status);
    }

    @Test
    void test_sendVerification_link_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698217240571,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user_52@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"Abhishek\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"test_user_52.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"15765255-aea0-4151-ab47-d575de172857\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osid\": \"89e2b4ab-490f-4eb5-9fb2-1222ceb7c42c\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',false,false,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        String requestBodyJson = verificationLinkRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFICATION_LINK_SEND).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_sendVerification_link_maxCount_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698217240571,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user_52@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"Abhishek\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"test_user_52.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"15765255-aea0-4151-ab47-d575de172857\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osid\": \"89e2b4ab-490f-4eb5-9fb2-1222ceb7c42c\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println(localDateTime);
        String query = String.format("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " +  " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',false,false,'successful',11,'%s','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')",localDateTime);
        postgreSQLClient.execute(query);
        String requestBodyJson = verificationLinkRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFICATION_LINK_SEND).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    void test_sendVerification_link_maxCount_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698217240571,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user_52@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"Abhishek\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"test_user_52.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"15765255-aea0-4151-ab47-d575de172857\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osid\": \"89e2b4ab-490f-4eb5-9fb2-1222ceb7c42c\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println(localDateTime);
        String query = String.format("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " +  " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',false,false,'successful',4,'%s','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')",localDateTime);
        postgreSQLClient.execute(query);
        String requestBodyJson = verificationLinkRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFICATION_LINK_SEND).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_sendVerification_link_invalid_request_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698217240571,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user_52@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"Abhishek\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"test_user_52.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"15765255-aea0-4151-ab47-d575de172857\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-03-16T10:09:03.826Z\",\n" +
                        "            \"osid\": \"89e2b4ab-490f-4eb5-9fb2-1222ceb7c42c\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        String requestBodyJson = verificationLinkRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFICATION_LINK_SEND).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    void test_sendVerification_link_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',false,false,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        String requestBodyJson = verificationLinkRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFICATION_LINK_SEND).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(500, status);
    }
    @Test
    void test_applicant_verify_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698220510818,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"participant_name\": \"test payor 1\",\n" +
                        "            \"primary_mobile\": \"9493347198\",\n" +
                        "            \"primary_email\": \"testpayor1@icici.com\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"address\": {\n" +
                        "                \"plot\": \"123\",\n" +
                        "                \"street\": \"Industrial Area\",\n" +
                        "                \"pincode\": \"560099\",\n" +
                        "                \"osid\": \"6a0b1b6c-2b34-4ec9-aedb-348f0599b71d\",\n" +
                        "                \"@type\": \"address\",\n" +
                        "                \"locality\": \"Anekal Taluk\"\n" +
                        "            },\n" +
                        "            \"phone\": [\n" +
                        "                18003012345\n" +
                        "            ],\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"endpoint_url\": \"http://a6a5d9138995a45b2bf9fd3f72b84367-915129339.ap-south-1.elb.amazonaws.com:8080/v0.7\",\n" +
                        "            \"payment_details\": {\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"osid\": \"c42464af-da72-446e-8503-ad3f9fa4937f\"\n" +
                        "            },\n" +
                        "            \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"participant_code\": \"testpayor1.icici@swasth-hcx-dev\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"df7d9449-7a78-4db0-aaaf-e0a946598ffd\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"testpayor1.icici@swasth-hcx-dev\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"@type\": \"Organisation\",\n" +
                        "            \"osUpdatedAt\": \"2023-09-13T12:39:25.420Z\",\n" +
                        "            \"sigining_cert_expiry\": 1840270798000\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        String requestBodyJson = applicantVerifyRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }
    @Test
    void test_applicant_verify_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("")
                .addHeader("Content-Type", "application/json"));
        String requestBodyJson = applicantVerifyRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(500, status);
    }

    @Test
    void test_applicant_verify_using_jwt_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('testhctes13.yopmail@swasth-hcx','testhctes13@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        String requestBodyJson = applicantVerifyWithJwtToken();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    void test_applicant_verify_link_expired_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('testhctes13.yopmail@swasth-hcx','testhctes13@yopmail.com','9620499129','169719173417','169719173417','10',false,true,'failed',0,'2023-10-12T13:37:12.533Z','5','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        String requestBodyJson = applicantVerifyWithJwtToken();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    void test_applicant_verify_record_doesnot_exists_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        String requestBodyJson = applicantVerifyWithJwtToken();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    void test_applicant_verify_link_verified_email_enabled() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698301336850,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"mock-invite@yopmail.com\",\n" +
                        "            \"mobile\": \"8522875773\",\n" +
                        "            \"username\": \"MockUser 43\",\n" +
                        "            \"user_name\": \"MockUser 43\",\n" +
                        "            \"user_id\": \"mock-invite@yopmail.com\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osid\": \"87f4fa26-bd88-4710-a296-0d580df2bd4b\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"created_by\": \"mock42@yopmail.com\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"f2ed8ea2-27ca-4f42-83c5-6ecdb3c735fc\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osid\": \"3ae89acd-21ab-4cab-a6e5-faf03587f3c2\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute(applicantVerifyOnboardVerificationTableData());
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('testhctes13@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','testhctes13.yopmail@swasth-hcx')");
        String requestBodyJson = applicantVerifyWithJwtToken();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_applicant_verify_emailPhone_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698301336850,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"mock-invite@yopmail.com\",\n" +
                        "            \"mobile\": \"8522875773\",\n" +
                        "            \"username\": \"MockUser 43\",\n" +
                        "            \"user_name\": \"MockUser 43\",\n" +
                        "            \"user_id\": \"mock-invite@yopmail.com\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osid\": \"87f4fa26-bd88-4710-a296-0d580df2bd4b\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"created_by\": \"mock42@yopmail.com\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"f2ed8ea2-27ca-4f42-83c5-6ecdb3c735fc\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osid\": \"3ae89acd-21ab-4cab-a6e5-faf03587f3c2\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute(applicantVerifyOnboardVerificationTableData());
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('testhctes13@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','testhctes13.yopmail@swasth-hcx')");
        String requestBodyJson = applicantVerifyWithJwtToken();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }
    @Test
    void test_emailPhone_enabled_applicant_verify_link_verified_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698301336850,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"mock-invite@yopmail.com\",\n" +
                        "            \"mobile\": \"8522875773\",\n" +
                        "            \"username\": \"MockUser 43\",\n" +
                        "            \"user_name\": \"MockUser 43\",\n" +
                        "            \"user_id\": \"mock-invite@yopmail.com\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osid\": \"87f4fa26-bd88-4710-a296-0d580df2bd4b\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"created_by\": \"mock42@yopmail.com\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"f2ed8ea2-27ca-4f42-83c5-6ecdb3c735fc\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osid\": \"3ae89acd-21ab-4cab-a6e5-faf03587f3c2\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute(applicantVerifyOnboardVerificationTableData());
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('testhctes13@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','testhctes13.yopmail@swasth-hcx')");
        String requestBodyJson = applicantVerifyWithJwtToken();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_email_not_enabled_applicant_verify_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698301336850,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"mock-invite@yopmail.com\",\n" +
                        "            \"mobile\": \"8522875773\",\n" +
                        "            \"username\": \"MockUser 43\",\n" +
                        "            \"user_name\": \"MockUser 43\",\n" +
                        "            \"user_id\": \"mock-invite@yopmail.com\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osid\": \"87f4fa26-bd88-4710-a296-0d580df2bd4b\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"created_by\": \"mock42@yopmail.com\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"f2ed8ea2-27ca-4f42-83c5-6ecdb3c735fc\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osid\": \"3ae89acd-21ab-4cab-a6e5-faf03587f3c2\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute(applicantVerifyOnboardVerificationTableData());
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('testhctes13@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','testhctes13.yopmail@swasth-hcx')");
        String requestBodyJson = applicantVerifyWithJwtToken();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_applicant_verify_link_verified_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698301336850,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"mock-invite@yopmail.com\",\n" +
                        "            \"mobile\": \"8522875773\",\n" +
                        "            \"username\": \"MockUser 43\",\n" +
                        "            \"user_name\": \"MockUser 43\",\n" +
                        "            \"user_id\": \"mock-invite@yopmail.com\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osid\": \"87f4fa26-bd88-4710-a296-0d580df2bd4b\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"created_by\": \"mock42@yopmail.com\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"f2ed8ea2-27ca-4f42-83c5-6ecdb3c735fc\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osid\": \"3ae89acd-21ab-4cab-a6e5-faf03587f3c2\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('testhctes13.yopmail@swasth-hcx','testhctes13@yopmail.com','9620499129','169719173417','169719173417','2555824693417',false,true,'failed',0,'2023-10-12T13:37:12.533Z','5','','','','{\"email\": \"verification\",\"phone\": \"activation\"}',' {\"email\": \"verification\",\"phone\": \"activation\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('testhctes13@yopmail.com','123445','987655','successful','1666612517000','1666612517000','testhctes13.yopmail@swasth-hcx')");
        String requestBodyJson = applicantVerifyWithJwtTokenFail();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_applicant_verify_link_mobile_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698301336850,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"mock-invite@yopmail.com\",\n" +
                        "            \"mobile\": \"8522875773\",\n" +
                        "            \"username\": \"MockUser 43\",\n" +
                        "            \"user_name\": \"MockUser 43\",\n" +
                        "            \"user_id\": \"mock-invite@yopmail.com\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osid\": \"87f4fa26-bd88-4710-a296-0d580df2bd4b\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"created_by\": \"mock42@yopmail.com\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"f2ed8ea2-27ca-4f42-83c5-6ecdb3c735fc\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osid\": \"3ae89acd-21ab-4cab-a6e5-faf03587f3c2\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute(applicantVerifyOnboardVerificationTable());
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('payr_hcxpyr_598881@swasth-hcx-dev','hcxpyr80@yopmail.com','9620499129','169719173417','169719173417','2555824693417',false,true,'failed',0,'2023-10-12T13:37:12.533Z','5','','','','{\"email\": \"verification\",\"phone\": \"activation\"}',' {\"email\": \"verification\",\"phone\": \"activation\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('testhctes13@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','testhctes13.yopmail@swasth-hcx')");
        String requestBodyJson = applicantVerifyJwtTokenMobile();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_applicant_verify_mobile_not_enabled_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698301336850,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"mock-invite@yopmail.com\",\n" +
                        "            \"mobile\": \"8522875773\",\n" +
                        "            \"username\": \"MockUser 43\",\n" +
                        "            \"user_name\": \"MockUser 43\",\n" +
                        "            \"user_id\": \"mock-invite@yopmail.com\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osid\": \"87f4fa26-bd88-4710-a296-0d580df2bd4b\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"created_by\": \"mock42@yopmail.com\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"f2ed8ea2-27ca-4f42-83c5-6ecdb3c735fc\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osid\": \"3ae89acd-21ab-4cab-a6e5-faf03587f3c2\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute(applicantVerifyOnboardVerificationTable());
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('payr_hcxpyr_598881@swasth-hcx-dev','hcxpyr80@yopmail.com','9620499129','169719173417','169719173417','2555824693417',true,true,'failed',0,'2023-10-12T13:37:12.533Z','5','','','','{\"email\": \"verification\",\"phone\": \"activation\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('testhctes13@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','testhctes13.yopmail@swasth-hcx')");
        String requestBodyJson = applicantVerifyJwtTokenMobile();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_applicant_verify_link_mobile_enabled_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698301336850,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"mock-invite@yopmail.com\",\n" +
                        "            \"mobile\": \"8522875773\",\n" +
                        "            \"username\": \"MockUser 43\",\n" +
                        "            \"user_name\": \"MockUser 43\",\n" +
                        "            \"user_id\": \"mock-invite@yopmail.com\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osid\": \"87f4fa26-bd88-4710-a296-0d580df2bd4b\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"created_by\": \"mock42@yopmail.com\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"f2ed8ea2-27ca-4f42-83c5-6ecdb3c735fc\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osid\": \"3ae89acd-21ab-4cab-a6e5-faf03587f3c2\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute(applicantVerifyOnboardVerificationTable());
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('payr_hcxpyr_598881@swasth-hcx-dev','hcxpyr80@yopmail.com','9620499129','169719173417','169719173417','2555824693417',false,true,'failed',0,'2023-10-12T13:37:12.533Z','5','','','','{\"email\": \"verification\",\"phone\": \"activation\"}',' {\"email\": \"activation\",\"phone\": \"activation\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('testhctes13@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','testhctes13.yopmail@swasth-hcx')");
        String requestBodyJson = applicantVerifyJwtTokenMobile();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_applicant_verify_invalid_jwtToken_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698391335312,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('testhctes13.yopmail@swasth-hcx','testhctes13@yopmail.com','9620499129','169719173417','169719173417','2555824693417',false,true,'failed',0,'2023-10-12T13:37:12.533Z','5','','','','{\"email\": \"verification\",\"phone\": \"activation\"}',' {\"email\": \"verification\",\"phone\": \"activation\"}')");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('payr_hcxpyr_598881@swasth-hcx-dev','hcxpyr80@yopmail.com','9620499129','169719173417','169719173417','2555824693417',false,true,'failed',0,'2023-10-12T13:37:12.533Z','5','','','','{\"email\": \"verification\",\"phone\": \"activation\"}',' {\"email\": \"activation\",\"phone\": \"activation\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('testhctes13@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','testhctes13.yopmail@swasth-hcx')");
        String requestBodyJson = applicantVerifyInvalidJwtToken();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    void test_applicant_verify_maxretry_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('testhctes13.yopmail@swasth-hcx','testhctes13@yopmail.com','9620499129','169719173417','169719173417','2555824693417',false,true,'failed',0,'2023-10-12T13:37:12.533Z','11','','','','{\"email\": \"verification\",\"phone\": \"activation\"}',' {\"email\": \"verification\",\"phone\": \"activation\"}')");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('payr_hcxpyr_598881@swasth-hcx-dev','hcxpyr80@yopmail.com','9620499129','169719173417','169719173417','2555824693417',false,true,'failed',0,'2023-10-12T13:37:12.533Z','11','','','','{\"email\": \"verification\",\"phone\": \"activation\"}',' {\"email\": \"activation\",\"phone\": \"activation\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('testhctes13@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','testhctes13.yopmail@swasth-hcx')");
        String requestBodyJson = applicantVerifyJwtTokenMobile();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }
    @Test
    void test_applicant_verify_link_verified_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698386158436,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('testhctes13.yopmail@swasth-hcx','testhctes13@yopmail.com','9620499129','169719173417','169719173417','2555824693417',true,true,'successful',0,'2023-10-12T13:37:12.533Z','5','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('testhctes13@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','testhctes13.yopmail@swasth-hcx')");
        String requestBodyJson = applicantVerifyWithJwtToken();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }
    @Test
    void test_applicant_get_info_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698220510818,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"participant_name\": \"test payor 1\",\n" +
                        "            \"primary_mobile\": \"9493347198\",\n" +
                        "            \"primary_email\": \"testpayor1@icici.com\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"address\": {\n" +
                        "                \"plot\": \"123\",\n" +
                        "                \"street\": \"Industrial Area\",\n" +
                        "                \"pincode\": \"560099\",\n" +
                        "                \"osid\": \"6a0b1b6c-2b34-4ec9-aedb-348f0599b71d\",\n" +
                        "                \"@type\": \"address\",\n" +
                        "                \"locality\": \"Anekal Taluk\"\n" +
                        "            },\n" +
                        "            \"phone\": [\n" +
                        "                18003012345\n" +
                        "            ],\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"endpoint_url\": \"http://a6a5d9138995a45b2bf9fd3f72b84367-915129339.ap-south-1.elb.amazonaws.com:8080/v0.7\",\n" +
                        "            \"payment_details\": {\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"osid\": \"c42464af-da72-446e-8503-ad3f9fa4937f\"\n" +
                        "            },\n" +
                        "            \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"participant_code\": \"testpayor1.icici@swasth-hcx-dev\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"df7d9449-7a78-4db0-aaaf-e0a946598ffd\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"testpayor1.icici@swasth-hcx-dev\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"@type\": \"Organisation\",\n" +
                        "            \"osUpdatedAt\": \"2023-09-13T12:39:25.420Z\",\n" +
                        "            \"sigining_cert_expiry\": 1840270798000\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        String requestBodyJson = applicantGetInfoRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_GET_INFO).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_applicant_get_info_jwt_token_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698220510818,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"participant_name\": \"test payor 1\",\n" +
                        "            \"primary_mobile\": \"9493347198\",\n" +
                        "            \"primary_email\": \"testpayor1@icici.com\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"address\": {\n" +
                        "                \"plot\": \"123\",\n" +
                        "                \"street\": \"Industrial Area\",\n" +
                        "                \"pincode\": \"560099\",\n" +
                        "                \"osid\": \"6a0b1b6c-2b34-4ec9-aedb-348f0599b71d\",\n" +
                        "                \"@type\": \"address\",\n" +
                        "                \"locality\": \"Anekal Taluk\"\n" +
                        "            },\n" +
                        "            \"phone\": [\n" +
                        "                18003012345\n" +
                        "            ],\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"endpoint_url\": \"http://a6a5d9138995a45b2bf9fd3f72b84367-915129339.ap-south-1.elb.amazonaws.com:8080/v0.7\",\n" +
                        "            \"payment_details\": {\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"osid\": \"c42464af-da72-446e-8503-ad3f9fa4937f\"\n" +
                        "            },\n" +
                        "            \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"participant_code\": \"testpayor1.icici@swasth-hcx-dev\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"df7d9449-7a78-4db0-aaaf-e0a946598ffd\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"testpayor1.icici@swasth-hcx-dev\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"@type\": \"Organisation\",\n" +
                        "            \"osUpdatedAt\": \"2023-09-13T12:39:25.420Z\",\n" +
                        "            \"sigining_cert_expiry\": 1840270798000\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        String requestBodyJson = applicantGetInfoJwtToken();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_GET_INFO).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    void test_applicant_get_info_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698227893490,\n" +
                        "    \"participants\": []\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        String requestBodyJson = applicantGetInfoRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_GET_INFO).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }
    @Test
     void test_onboard_user_invite_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698227893490,\n" +
                        "    \"users\": []\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698228836794,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"participant_name\": \"test provider 1\",\n" +
                        "            \"primary_mobile\": \"9493347198\",\n" +
                        "            \"primary_email\": \"testprovider1@apollo.com\",\n" +
                        "            \"roles\": [\n" +
                        "                \"provider\"\n" +
                        "            ],\n" +
                        "            \"address\": {\n" +
                        "                \"plot\": \"123\",\n" +
                        "                \"street\": \"Industrial Area\",\n" +
                        "                \"landmark\": \"Health City\",\n" +
                        "                \"village\": \"Nampally\",\n" +
                        "                \"district\": \"Bengaluru\",\n" +
                        "                \"state\": \"Karnataka\",\n" +
                        "                \"pincode\": \"560099\",\n" +
                        "                \"osid\": \"e2234870-1dbb-40c4-bceb-69aedeb0f61a\",\n" +
                        "                \"@type\": \"address\",\n" +
                        "                \"locality\": \"Anekal Taluk\"\n" +
                        "            },\n" +
                        "            \"phone\": [\n" +
                        "                18003012345\n" +
                        "            ],\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"endpoint_url\": \"http://a6a5d9138995a45b2bf9fd3f72b84367-915129339.ap-south-1.elb.amazonaws.com:8080/v0.7\",\n" +
                        "            \"payment_details\": {\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"osid\": \"fa29f63c-7c07-45cb-9719-ce7d9e4d3b77\"\n" +
                        "            },\n" +
                        "            \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"8527853c-b442-44db-aeda-dbbdcf472d9b\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "            \"@type\": \"Organisation\",\n" +
                        "            \"payment\": {\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"@type\": \"payment_details\",\n" +
                        "                \"osid\": \"fa29f63c-7c07-45cb-9719-ce7d9e4d3b77\"\n" +
                        "            },\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"tag\": [\n" +
                        "                \"hcx-workshop-1\"\n" +
                        "            ],\n" +
                        "            \"tags\": [\n" +
                        "                \"hcx-workshop-2\",\n" +
                        "                \"hcx-workshop-1\"\n" +
                        "            ],\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:41:06.082Z\",\n" +
                        "            \"sigining_cert_expiry\": 1840270798000\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_user_invite_details");
        postgreSQLClient.execute("CREATE TABLE onboard_user_invite_details(participant_code character varying,user_email character varying,invited_by character varying,invite_status character varying ,created_on bigInt)");
        String requestBodyJson = onboardUserInviteRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.ONBOARD_USER_INVITE).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_onboard_invite_with_jwtToken_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698647527393,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_user_invite_details");
        postgreSQLClient.execute("CREATE TABLE onboard_user_invite_details(participant_code character varying,user_email character varying,invited_by character varying,invite_status character varying ,created_on bigInt)");
        String requestBodyJson = onboardUserInviteJwtTokenException();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.ONBOARD_USER_INVITE_ACCEPT).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    void test_onboard_invite_with_jwtToken_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698647527393,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:42:08.924Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698227893490,\n" +
                        "    \"users\": []\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698649743030,\n" +
                        "    \"user_id\": \"mock-invite@yopmail.com\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"result\": [\n" +
                        "        {\n" +
                        "            \"user_id\": \"mock-invite@yopmail.com\",\n" +
                        "            \"status\": \"successful\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"overallStatus\": \"successful\",\n" +
                        "    \"timestamp\": 1698650224041\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698655981554,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"testhctes13@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test-payor\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"testhctes13.yopmail@swasth-hcx\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"24a0835a-42b7-40e1-ab9c-f704e0e9da1b\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-07-28T06:29:43.893Z\",\n" +
                        "            \"osid\": \"358ec012-c448-4f4c-9513-40ff8ea20fa9\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_user_invite_details");
        postgreSQLClient.execute("CREATE TABLE onboard_user_invite_details(participant_code character varying,user_email character varying,invited_by character varying,invite_status character varying ,created_on bigInt,updated_on bigInt)");
        Mockito.doNothing().when(kafkaClient).send(anyString(),anyString(),anyString());
        Mockito.when(mockEventGenerator.getEmailMessageEvent(anyString(),anyString(),anyList(),anyList(),anyList())).thenReturn("mocked-event");
        Mockito.when(freemarkerService.renderTemplate(any(),anyMap())).thenReturn("freemarker");
        String requestBodyJson = onboardUserInviteJwtToken();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.ONBOARD_USER_INVITE_ACCEPT).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_onboard_user_invite_user_exists_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698301336850,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"mock-invite@yopmail.com\",\n" +
                        "            \"mobile\": \"8522875773\",\n" +
                        "            \"username\": \"MockUser 43\",\n" +
                        "            \"user_name\": \"MockUser 43\",\n" +
                        "            \"user_id\": \"mock-invite@yopmail.com\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "                    \"role\": \"admin\",\n" +
                        "                    \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "                    \"osid\": \"87f4fa26-bd88-4710-a296-0d580df2bd4b\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"created_by\": \"mock42@yopmail.com\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"f2ed8ea2-27ca-4f42-83c5-6ecdb3c735fc\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-06-21T07:45:54.100Z\",\n" +
                        "            \"osid\": \"3ae89acd-21ab-4cab-a6e5-faf03587f3c2\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_user_invite_details");
        postgreSQLClient.execute("CREATE TABLE onboard_user_invite_details(participant_code character varying,user_email character varying,invited_by character varying,invite_status character varying ,created_on bigInt)");
        String requestBodyJson = onboardUserInviteRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.ONBOARD_USER_INVITE).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    void test_onboard_user_invite_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698227893490,\n" +
                        "    \"participants\": []\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_user_invite_details");
        postgreSQLClient.execute("CREATE TABLE onboard_user_invite_details(participant_code character varying,user_email character varying,invited_by character varying,invite_status character varying ,created_on bigInt)");
        String requestBodyJson = onboardUserInviteRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.ONBOARD_USER_INVITE).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(500, status);
    }

    @Test
    void test_applicant_search_with_filters_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698382273219,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"participant_name\": \"swasth mock provider dev\",\n" +
                        "            \"primary_mobile\": \"9493347198\",\n" +
                        "            \"primary_email\": \"swasthmockproviderdev@gmail.com\",\n" +
                        "            \"roles\": [\n" +
                        "                \"provider\"\n" +
                        "            ],\n" +
                        "            \"address\": {\n" +
                        "                \"plot\": \"5-4-199\",\n" +
                        "                \"street\": \"road no 12\",\n" +
                        "                \"landmark\": \"Jawaharlal Nehru Road\",\n" +
                        "                \"village\": \"Nampally\",\n" +
                        "                \"district\": \"Hyderabad\",\n" +
                        "                \"state\": \"Telangana\",\n" +
                        "                \"pincode\": \"500805\",\n" +
                        "                \"osid\": \"df17d5bd-33da-4b20-a3c3-f159617d17d2\",\n" +
                        "                \"@type\": \"address\"\n" +
                        "            },\n" +
                        "            \"phone\": [\n" +
                        "                \"040-387658992\"\n" +
                        "            ],\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"endpoint_url\": \"http://ad3438079870f497093baf41fd7bd763-1439847685.ap-south-1.elb.amazonaws.com:8000/v1\",\n" +
                        "            \"payment_details\": {\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"osid\": \"6dae8930-d30d-474e-b0a8-d3c43e050a1e\"\n" +
                        "            },\n" +
                        "            \"signing_cert_path\": \"urn:isbn:0-476-27557-4\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                \"22344\"\n" +
                        "            ],\n" +
                        "            \"encryption_cert\": \"urn:isbn:0-4234\",\n" +
                        "            \"participant_code\": \"provider-swasth-mock-provider-dev\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"ebadaf81-3517-4545-9fb2-8cf0aa9723b3\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"provider-swasth-mock-provider-dev\",\n" +
                        "            \"@type\": \"Organisation\",\n" +
                        "            \"payment\": {\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"@type\": \"payment_details\",\n" +
                        "                \"osid\": \"6dae8930-d30d-474e-b0a8-d3c43e050a1e\"\n" +
                        "            }\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS mock_participant");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('provider-swasth-mock-provider-dev','swasthmockproviderdev@gmail.com','9620499129','169719173417','169719173417','2555824693417',true,false,'successful',0,'2023-10-12T13:37:12.533Z','5','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('swasthmockproviderdev@gmail.com','123445','987655','accepted','1666612517000','1666612517000','provider-swasth-mock-provider-dev')");
        postgreSQLClient.execute("CREATE TABLE  mock_participant(parent_participant_code character varying,  child_participant_code character varying NOT NULL PRIMARY KEY ,primary_email character varying, password character varying,private_key character varying)");
        postgreSQLClient.execute("INSERT INTO mock_participant(parent_participant_code,child_participant_code,primary_email,password,private_key)"+"VALUES ('provider-swasth-mock-provider-dev','test-mock-99+mock_payor@yopmail.com','swasthmockproviderdev@gmail.com','Fgkt#54kfgkN','MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDX8KLtg3R9jiqy0GHNdPgo7Kb+eOQlBeWFHAKXY/1TG3m6CaVwtQzof3akE457WFtGR1bP41/0AzDIvmKTVXQCwer9wB2GYuQfrUjvPbFgECcBOC3Dv0Ak/3+T05ntnVrwxYLowEQl53IQt7IWOUCLIBNYjn3B6Wx99PPY4UcHSL6HoWN4/KnAKJiHcUtMSuIUfAG4BMtWDt1yKTRBPNwZpwhka0NTTnPnfVP5z3amtC1o6jWsNNntXPqyR5CNQn8BLbaMtnzQG4K8wfY7rPIgr7mYCteIoEpEMkPu4viBAGUlO047ybugbSRSP28D79AP9QlgI3Qn4bB9wmzTi9UtAgMBAAECggEBAJfrYQTOdfcbPOj+d8BPKYPJMpdXP9LYOiiSkzQlEYUVkGcVAEKx7XnoqvQ2GginGdfwup+ZLNmEIR8p6joTZYHHIecR8POpwSqUA/rkoVSfKIHQH0pW0+7znbLHrMSh7ufzXO0YzxkHopUmV3ERKFp434NvBASXj09yNNgBbbIt7WKTqjuNA/lOY+VfhkSxoxVMnRJYtq3vbm8x/+QU1qthUpt6UbbGHTOHMl2MsS/MRPLZUbG00E/nhyH5/w+ZOApHCQc2ZTuXCqhRqTDkeWvgdnfX/pxsVvhBAq5MltlyhBiKUGB90PRISJ/Wu6462ywiiK9zUozCyDXtWQJ8QkECgYEA9FhyEY8Sp9tKPQECIRCFXIYpv8kAuTbw+1sgi10SBcDPSLY1HEO7RZu94+G3CygnD3bEaCzaWLQ0FlbPB4wKl1bBoAY735LoAcZR0nc8zTbm4nLl0ibu2NUzRfpW6X1dgFmuHUpcbAr2SWLoec1y5OvwScplOcBQXK7r6vPfPdECgYEA4j1ZNpFDwHxxxSAs1tNZlhAz1DFPw+LRb+Ap34UaMWA18ZGtvKwzZfFrtbGSYhu05SO+sYZbk4Z1s84gCtj8HqiUFFvI8v0ozwPRPEzFaipGcVNFRCornoxcv6EC3x1CFhlowHt2MphADfRQA7zlJk/HIq65DD9weXzMr4aTLJ0CgYEAkueiHSBxzO2w4qB6kTqHk6st6pqEjtaTZ+vP0zovnbngZgz2PXoTW7RZJGsOS+zmHwv+5cshs3cUYeHrMtRlgbutSfK1iKOgTYDYrLr3mUHK6pa9ye2SaFc2LnpmSpcO4h4I6p9MlcC5dkG7F5AH5c5cd2DyHxiauD6KpIXe0CECgYAxCXUV08SoqxCJ1qCBa8wGL7rcKlgMsFQO+Lp6vUHhI+ZtVtMeiwCU/xAGkNeWtkSuSeIiXmno/wLyFyJw13lGN+now8A5k')");
        postgreSQLClient.execute("INSERT INTO mock_participant(parent_participant_code,child_participant_code,primary_email,password,private_key)"+"VALUES ('provider-swasth-mock-provider-dev','test-mock-06+mock_provider@yopmail.com','swasthmockproviderdev@gmail.com','Fgkt#54kfgkN','MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDX8KLtg3R9jiqy0GHNdPgo7Kb+eOQlBeWFHAKXY/1TG3m6CaVwtQzof3akE457WFtGR1bP41/0AzDIvmKTVXQCwer9wB2GYuQfrUjvPbFgECcBOC3Dv0Ak/3+T05ntnVrwxYLowEQl53IQt7IWOUCLIBNYjn3B6Wx99PPY4UcHSL6HoWN4/KnAKJiHcUtMSuIUfAG4BMtWDt1yKTRBPNwZpwhka0NTTnPnfVP5z3amtC1o6jWsNNntXPqyR5CNQn8BLbaMtnzQG4K8wfY7rPIgr7mYCteIoEpEMkPu4viBAGUlO047ybugbSRSP28D79AP9QlgI3Qn4bB9wmzTi9UtAgMBAAECggEBAJfrYQTOdfcbPOj+d8BPKYPJMpdXP9LYOiiSkzQlEYUVkGcVAEKx7XnoqvQ2GginGdfwup+ZLNmEIR8p6joTZYHHIecR8POpwSqUA/rkoVSfKIHQH0pW0+7znbLHrMSh7ufzXO0YzxkHopUmV3ERKFp434NvBASXj09yNNgBbbIt7WKTqjuNA/lOY+VfhkSxoxVMnRJYtq3vbm8x/+QU1qthUpt6UbbGHTOHMl2MsS/MRPLZUbG00E/nhyH5/w+ZOApHCQc2ZTuXCqhRqTDkeWvgdnfX/pxsVvhBAq5MltlyhBiKUGB90PRISJ/Wu6462ywiiK9zUozCyDXtWQJ8QkECgYEA9FhyEY8Sp9tKPQECIRCFXIYpv8kAuTbw+1sgi10SBcDPSLY1HEO7RZu94+G3CygnD3bEaCzaWLQ0FlbPB4wKl1bBoAY735LoAcZR0nc8zTbm4nLl0ibu2NUzRfpW6X1dgFmuHUpcbAr2SWLoec1y5OvwScplOcBQXK7r6vPfPdECgYEA4j1ZNpFDwHxxxSAs1tNZlhAz1DFPw+LRb+Ap34UaMWA18ZGtvKwzZfFrtbGSYhu05SO+sYZbk4Z1s84gCtj8HqiUFFvI8v0ozwPRPEzFaipGcVNFRCornoxcv6EC3x1CFhlowHt2MphADfRQA7zlJk/HIq65DD9weXzMr4aTLJ0CgYEAkueiHSBxzO2w4qB6kTqHk6st6pqEjtaTZ+vP0zovnbngZgz2PXoTW7RZJGsOS+zmHwv+5cshs3cUYeHrMtRlgbutSfK1iKOgTYDYrLr3mUHK6pa9ye2SaFc2LnpmSpcO4h4I6p9MlcC5dkG7F5AH5c5cd2DyHxiauD6KpIXe0CECgYAxCXUV08SoqxCJ1qCBa8wGL7rcKlgMsFQO+Lp6vUHhI+ZtVtMeiwCU/xAGkNeWtkSuSeIiXmno/wLyFyJw13lGN+now8A5k')");
        String requestBodyJson = applicantSearchRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_SEARCH +"?fields=communication,sponsors,onboard_validation_properties,mockparticipants").content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_applicant_search_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698382273219,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"participant_name\": \"swasth mock provider dev\",\n" +
                        "            \"primary_mobile\": \"9493347198\",\n" +
                        "            \"primary_email\": \"swasthmockproviderdev@gmail.com\",\n" +
                        "            \"roles\": [\n" +
                        "                \"provider\"\n" +
                        "            ],\n" +
                        "            \"address\": {\n" +
                        "                \"plot\": \"5-4-199\",\n" +
                        "                \"street\": \"road no 12\",\n" +
                        "                \"landmark\": \"Jawaharlal Nehru Road\",\n" +
                        "                \"village\": \"Nampally\",\n" +
                        "                \"district\": \"Hyderabad\",\n" +
                        "                \"state\": \"Telangana\",\n" +
                        "                \"pincode\": \"500805\",\n" +
                        "                \"osid\": \"df17d5bd-33da-4b20-a3c3-f159617d17d2\",\n" +
                        "                \"@type\": \"address\"\n" +
                        "            },\n" +
                        "            \"phone\": [\n" +
                        "                \"040-387658992\"\n" +
                        "            ],\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"endpoint_url\": \"http://ad3438079870f497093baf41fd7bd763-1439847685.ap-south-1.elb.amazonaws.com:8000/v1\",\n" +
                        "            \"payment_details\": {\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"osid\": \"6dae8930-d30d-474e-b0a8-d3c43e050a1e\"\n" +
                        "            },\n" +
                        "            \"signing_cert_path\": \"urn:isbn:0-476-27557-4\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                \"22344\"\n" +
                        "            ],\n" +
                        "            \"encryption_cert\": \"urn:isbn:0-4234\",\n" +
                        "            \"participant_code\": \"provider-swasth-mock-provider-dev\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"ebadaf81-3517-4545-9fb2-8cf0aa9723b3\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"provider-swasth-mock-provider-dev\",\n" +
                        "            \"@type\": \"Organisation\",\n" +
                        "            \"payment\": {\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"@type\": \"payment_details\",\n" +
                        "                \"osid\": \"6dae8930-d30d-474e-b0a8-d3c43e050a1e\"\n" +
                        "            }\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        String requestBodyJson = applicantSearchRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_SEARCH ).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void test_applicant_search_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698382273219,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"participant_name\": \"swasth mock provider dev\",\n" +
                        "            \"primary_mobile\": \"9493347198\",\n" +
                        "            \"primary_email\": \"swasthmockproviderdev@gmail.com\",\n" +
                        "            \"roles\": [\n" +
                        "                \"provider\"\n" +
                        "            ],\n" +
                        "            \"address\": {\n" +
                        "                \"plot\": \"5-4-199\",\n" +
                        "                \"street\": \"road no 12\",\n" +
                        "                \"landmark\": \"Jawaharlal Nehru Road\",\n" +
                        "                \"village\": \"Nampally\",\n" +
                        "                \"district\": \"Hyderabad\",\n" +
                        "                \"state\": \"Telangana\",\n" +
                        "                \"pincode\": \"500805\",\n" +
                        "                \"osid\": \"df17d5bd-33da-4b20-a3c3-f159617d17d2\",\n" +
                        "                \"@type\": \"address\"\n" +
                        "            },\n" +
                        "            \"phone\": [\n" +
                        "                \"040-387658992\"\n" +
                        "            ],\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"endpoint_url\": \"http://ad3438079870f497093baf41fd7bd763-1439847685.ap-south-1.elb.amazonaws.com:8000/v1\",\n" +
                        "            \"payment_details\": {\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"osid\": \"6dae8930-d30d-474e-b0a8-d3c43e050a1e\"\n" +
                        "            },\n" +
                        "            \"signing_cert_path\": \"urn:isbn:0-476-27557-4\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                \"22344\"\n" +
                        "            ],\n" +
                        "            \"encryption_cert\": \"urn:isbn:0-4234\",\n" +
                        "            \"participant_code\": \"provider-swasth-mock-provider-dev\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"ebadaf81-3517-4545-9fb2-8cf0aa9723b3\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"provider-swasth-mock-provider-dev\",\n" +
                        "            \"@type\": \"Organisation\",\n" +
                        "            \"payment\": {\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"@type\": \"payment_details\",\n" +
                        "                \"osid\": \"6dae8930-d30d-474e-b0a8-d3c43e050a1e\"\n" +
                        "            }\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verifier");
        postgreSQLClient.execute("DROP TABLE IF EXISTS mock_participant");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count int, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " + " VALUES('provider-swasth-mock-provider-dev','swasthmockproviderdev@gmail.com','9620499129','169719173417','169719173417','2555824693417',true,false,'successful',0,'2023-10-12T13:37:12.533Z','5','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('swasthmockproviderdev@gmail.com','123445','987655','accepted','1666612517000','1666612517000','provider-swasth-mock-provider-dev')");
        String requestBodyJson = applicantSearchRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_SEARCH +"?fields=communication,sponsors,onboard_validation_properties,mockparticipants").content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }
    @Test
    void test_invite_reject_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698231368796,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"participant_name\": \"test provider 1\",\n" +
                        "            \"primary_mobile\": \"9493347198\",\n" +
                        "            \"primary_email\": \"testprovider1@apollo.com\",\n" +
                        "            \"roles\": [\n" +
                        "                \"provider\"\n" +
                        "            ],\n" +
                        "            \"address\": {\n" +
                        "                \"plot\": \"123\",\n" +
                        "                \"street\": \"Industrial Area\",\n" +
                        "                \"landmark\": \"Health City\",\n" +
                        "                \"village\": \"Nampally\",\n" +
                        "                \"district\": \"Bengaluru\",\n" +
                        "                \"state\": \"Karnataka\",\n" +
                        "                \"pincode\": \"560099\",\n" +
                        "                \"osid\": \"e2234870-1dbb-40c4-bceb-69aedeb0f61a\",\n" +
                        "                \"@type\": \"address\",\n" +
                        "                \"locality\": \"Anekal Taluk\"\n" +
                        "            },\n" +
                        "            \"phone\": [\n" +
                        "                18003012345\n" +
                        "            ],\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"endpoint_url\": \"http://a6a5d9138995a45b2bf9fd3f72b84367-915129339.ap-south-1.elb.amazonaws.com:8080/v0.7\",\n" +
                        "            \"payment_details\": {\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"osid\": \"fa29f63c-7c07-45cb-9719-ce7d9e4d3b77\"\n" +
                        "            },\n" +
                        "            \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"8527853c-b442-44db-aeda-dbbdcf472d9b\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "            \"@type\": \"Organisation\",\n" +
                        "            \"payment\": {\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"@type\": \"payment_details\",\n" +
                        "                \"osid\": \"fa29f63c-7c07-45cb-9719-ce7d9e4d3b77\"\n" +
                        "            },\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osUpdatedAt\": \"2023-10-25T05:41:06.082Z\",\n" +
                        "            \"sigining_cert_expiry\": 1840270798000\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        String requestBodyJson = userInviteRejectException();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.ONBOARD_USER_INVITE_REJECT).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    void onboard_applicant_password_generate_exception() throws Exception {
        String requestBodyJson = applicantPasswordRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.ONBOARD_APPLICANT_PASSWORD_GENERATE).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    void user_invite_reject_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698693054350,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"participant_name\": \"Health Claims Exchange Gateway\",\n" +
                        "            \"endpoint_url\": \"http://dev-hcx.swasth.app/\",\n" +
                        "            \"roles\": [\n" +
                        "                \"HIE/HIO.HCX\"\n" +
                        "            ],\n" +
                        "            \"primary_email\": \"hcxgateway@swasth.org\",\n" +
                        "            \"primary_mobile\": \"\",\n" +
                        "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                        "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/hcxgateway.swasth%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"participant_code\": \"hcxgateway.swasth@swasth-hcx-dev\",\n" +
                        "            \"sigining_cert_expiry\": 1993808205000,\n" +
                        "            \"encryption_cert_expiry\": 1993808205000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"59d43f9b-42e7-4480-9ec2-aa6bd95ccb5f\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-03-10T12:05:37.736Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-10-30T12:03:42.356Z\",\n" +
                        "            \"osid\": \"c776b615-b6cb-481c-8a78-de0c0c80f38a\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698693740790,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"participant_name\": \"test provider 1\",\n" +
                        "            \"primary_mobile\": \"9493347198\",\n" +
                        "            \"primary_email\": \"testprovider1@apollo.com\",\n" +
                        "            \"roles\": [\n" +
                        "                \"provider\"\n" +
                        "            ],\n" +
                        "            \"address\": {\n" +
                        "                \"plot\": \"123\",\n" +
                        "                \"street\": \"Industrial Area\",\n" +
                        "                \"landmark\": \"Health City\",\n" +
                        "                \"village\": \"Nampally\",\n" +
                        "                \"district\": \"Bengaluru\",\n" +
                        "                \"state\": \"Karnataka\",\n" +
                        "                \"pincode\": \"560099\",\n" +
                        "                \"osid\": \"e2234870-1dbb-40c4-bceb-69aedeb0f61a\",\n" +
                        "                \"@type\": \"address\",\n" +
                        "                \"locality\": \"Anekal Taluk\"\n" +
                        "            },\n" +
                        "            \"phone\": [\n" +
                        "                18003012345\n" +
                        "            ],\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"endpoint_url\": \"http://a6a5d9138995a45b2bf9fd3f72b84367-915129339.ap-south-1.elb.amazonaws.com:8080/v0.7\",\n" +
                        "            \"payment_details\": {\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"osid\": \"fa29f63c-7c07-45cb-9719-ce7d9e4d3b77\"\n" +
                        "            },\n" +
                        "            \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"linked_registry_codes\": [\n" +
                        "                12345\n" +
                        "            ],\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"8527853c-b442-44db-aeda-dbbdcf472d9b\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"testprovider1.apollo@swasth-hcx-dev\",\n" +
                        "            \"@type\": \"Organisation\",\n" +
                        "            \"payment\": {\n" +
                        "                \"ifsc_code\": \"ICICI\",\n" +
                        "                \"account_number\": \"4707890099809809\",\n" +
                        "                \"@type\": \"payment_details\",\n" +
                        "                \"osid\": \"fa29f63c-7c07-45cb-9719-ce7d9e4d3b77\"\n" +
                        "            },\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"tag\": [\n" +
                        "                \"hcx-workshop-1\"\n" +
                        "            ],\n" +
                        "            \"tags\": [\n" +
                        "                \"hcx-workshop-2\",\n" +
                        "                \"hcx-workshop-1\"\n" +
                        "            ],\n" +
                        "            \"osUpdatedAt\": \"2023-10-30T12:03:42.096Z\",\n" +
                        "            \"sigining_cert_expiry\": 1840270798000\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_user_invite_details");
        postgreSQLClient.execute("CREATE TABLE onboard_user_invite_details(participant_code character varying,user_email character varying,invited_by character varying,invite_status character varying ,created_on bigInt , updated_on bigInt)");
        String requestBodyJson = userInviteReject();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.ONBOARD_USER_INVITE_REJECT).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    void onboard_applicant_password_generate_success() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698733663581,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"participant_name\": \"hcx test\",\n" +
                        "            \"primary_email\": \"hcxtestprovider9000@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"8522875773\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\",\n" +
                        "            \"status\": \"Active\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"participant_code\": \"hcxtestprovider9000.yopmail@swasth-hcx-dev\",\n" +
                        "            \"encryption_cert_expiry\": 1840270798000,\n" +
                        "            \"osOwner\": [\n" +
                        "                \"a9f33168-a004-4486-9c18-b3edb8f96317\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-08-06T13:20:33.819Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-08-06T13:26:05.043Z\",\n" +
                        "            \"osid\": \"a4583d62-7ed2-4a8c-93cc-b2f94f9205eb\",\n" +
                        "            \"@type\": \"Organisation\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1698733290848,\n" +
                        "    \"users\": [\n" +
                        "        {\n" +
                        "            \"email\": \"hcxtestprovider9000@yopmail.com\",\n" +
                        "            \"mobile\": \"8522875773\",\n" +
                        "            \"user_name\": \"hcx test Admin\",\n" +
                        "            \"user_id\": \"hcxtestprovider9000@yopmail.com\",\n" +
                        "            \"tenant_roles\": [\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-08-06T13:56:26.729Z\",\n" +
                        "                    \"role\": \"config-manager\",\n" +
                        "                    \"participant_code\": \"hcxtestprovider9000.yopmail@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"6919f2d7-0842-46f6-a38f-3d5b798c978d\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"created_by\": \"hcxtestprovider9000.yopmail@swasth-hcx-dev\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"732c64c7-f549-4910-8615-a161697baaf9\"\n" +
                        "            ],\n" +
                        "            \"osCreatedAt\": \"2023-08-06T13:20:34.035Z\",\n" +
                        "            \"osUpdatedAt\": \"2023-08-06T13:56:26.855Z\",\n" +
                        "            \"osid\": \"3ae8554b-6a99-4b5c-b317-6cdcbcfc4a68\",\n" +
                        "            \"@type\": \"User\",\n" +
                        "            \"tenant\": [\n" +
                        "                {\n" +
                        "                    \"osUpdatedAt\": \"2023-08-06T13:56:26.855Z\",\n" +
                        "                    \"role\": \"config-manager\",\n" +
                        "                    \"participant_code\": \"hcxtestprovider9000.yopmail@swasth-hcx-dev\",\n" +
                        "                    \"osid\": \"6919f2d7-0842-46f6-a38f-3d5b798c978d\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        String requestBodyJson = applicantPasswordRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.ONBOARD_APPLICANT_PASSWORD_GENERATE).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,authorizationHeaderForGeneratePassword()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }
}
