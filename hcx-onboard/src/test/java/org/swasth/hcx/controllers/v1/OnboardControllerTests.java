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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.swasth.common.utils.Constants.SUCCESSFUL;

class OnboardControllerTests extends BaseSpec{
    @Autowired
    private OnboardController onboardController;
    @Mock
    private OnboardService onboardService;

    @Test
    public void testVerify() throws Exception {
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
    public void testVerify_user_exists() throws Exception {
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
    public void testVerifyPayor() throws Exception {
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
    public void test_sendVerification_link_moreThan1count() throws Exception {
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
    public void test_verify_exception() throws Exception {
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
    public void test_onboard_update() throws Exception {
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
        Mockito.doNothing().when(onboardService).setKeycloakPassword(anyString(),anyString(),anyMap());
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
        Assertions.assertEquals(200, status);
    }

    @Test
    public void test_onboard_update_exception() throws Exception {
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
        Mockito.doNothing().when(onboardService).setKeycloakPassword(anyString(),anyString(),anyMap());
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
    public void test_verify_identity() throws Exception {
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
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " +
                " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('test_user_52@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','test_user_52.yopmail@swasth-hcx')");
        String requestBodyJson = verifyIdentityRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY_IDENTITY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    public void test_verify_identity_reject() throws Exception {
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
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " +
                " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('test_user_52@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','test_user_52.yopmail@swasth-hcx')");
        String requestBodyJson = verifyIdentityRejectRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY_IDENTITY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    public void test_verify_identity_invalid_status() throws Exception {
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
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " +
                " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('test_user_52@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','test_user_52.yopmail@swasth-hcx')");
        String requestBodyJson = verifyIdentityOtherThanAllowedStatus();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY_IDENTITY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    public void test_verify_identity_exception() throws Exception {
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
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " +
                " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('test_user_52@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','test_user_52.yopmail@swasth-hcx')");
        String requestBodyJson = verifyIdentityRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY_IDENTITY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(500, status);
    }

    @Test
    public void test_sendVerification_link() throws Exception {
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
                " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',false,false,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        String requestBodyJson = verificationLinkRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFICATION_LINK_SEND).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    public void test_sendVerification_link_maxCount_exception() throws Exception {
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
                " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',false,false,'successful',11,'2023-10-26T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        String requestBodyJson = verificationLinkRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFICATION_LINK_SEND).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    public void test_sendVerification_link_invalid_request_exception() throws Exception {
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
//        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " +
//                " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',false,false,'successful',11,'2023-10-26T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        String requestBodyJson = verificationLinkRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFICATION_LINK_SEND).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    public void test_sendVerification_link_exception() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("DROP TABLE IF EXISTS onboard_verification");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " +
                " VALUES('test_user_52.yopmail@swasth-hcx','test_user_52@yopmail.com','9620499129','169719173417','169719173417','1666612517000',false,false,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        String requestBodyJson = verificationLinkRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFICATION_LINK_SEND).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(500, status);
    }
    @Test
    public void test_applicant_verify() throws Exception {
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
    public void test_applicant_verify_exception() throws Exception {
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
    public void test_applicant_get_info() throws Exception {
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
    public void test_applicant_get_info_exception() throws Exception {
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
    public void test_onboard_user_invite() throws Exception {
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
    public void test_onboard_user_invite_user_exists_exception() throws Exception {
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
    public void test_onboard_user_invite_exception() throws Exception {
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

//    @Test
//    public void test_applicant_search() throws Exception {
//        String requestBodyJson = applicantSearchRequestBody();
//        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_SEARCH,"?fields=communication").content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
//        MockHttpServletResponse response = mvcResult.getResponse();
//        int status = response.getStatus();
//        Assertions.assertEquals(200, status);
//    }

    @Test
    public void test_invite_reject_exception() throws Exception {
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
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.ONBOARD_USER_INVITE_REJECT,"?fields=communication").content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }

    @Test
    public void test_invite_accept_exception() throws Exception {
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
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.ONBOARD_USER_INVITE_ACCEPT,"?fields=communication").content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }
    @Test
    public void onboard_applicant_password_generate() throws Exception {
        String requestBodyJson = applicantPasswordRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.ONBOARD_APPLICANT_PASSWORD_GENERATE).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }
}
