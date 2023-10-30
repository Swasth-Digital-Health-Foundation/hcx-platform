package org.swasth.hcx.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.dto.User;
import org.swasth.hcx.config.GenericConfiguration;
import org.swasth.hcx.helpers.EventGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.swasth.common.exception.ErrorCodes.INTERNAL_SERVER_ERROR;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Import(GenericConfiguration.class)
public class EventGeneratorTests {
    @Autowired
    protected WebApplicationContext wac;
    protected MockMvc mockMvc;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Autowired
    private EventGenerator eventGenerator; // Replace with the actual service class that contains your method

    @Test
    @Category(EventGenerator.class)
    public void testGetVerifyLinkEvent() {
        Map<String, Object> request = new HashMap<>();
        request.put("PARTICIPANT_CODE", "SampleParticipantCode");
        int attemptCount = 2;
        boolean emailOtpVerified = true;
        boolean phoneOtpVerified = false;
        Map<String, Object> event = eventGenerator.getVerifyLinkEvent(request, attemptCount, emailOtpVerified, phoneOtpVerified);
        assertNotNull(event);
    }

    @Test
    void testGetSendLinkEvent() {
        Map<String, Object> request = new HashMap<>();
        request.put("PARTICIPANT_NAME", "John Doe");
        request.put("PRIMARY_EMAIL", "johndoe@example.com");
        request.put("PRIMARY_MOBILE", "1234567890");
        request.put("PARTICIPANT_CODE", "P123");
        int regenCount = 3;
        LocalDate regenDate = LocalDate.of(2023, 10, 31);
        Map<String, Object> event = eventGenerator.getSendLinkEvent(request, regenCount, regenDate);
        assertNotNull(event);
    }

    @Test
    void testGetIdentityVerifyEvent() {
        // Create a sample request map, result, and error
        Map<String, Object> request = new HashMap<>();
        request.put("APPLICANT_CODE", "A123");
        request.put("VERIFIER_CODE", "V456");
        request.put("EMAIL", "user@example.com");
        request.put("MOBILE", "1234567890");
        request.put("APPLICANT_NAME", "John Doe");
        ArrayList<String> additionalVerification = new ArrayList<>();
        additionalVerification.add("Verification1");
        additionalVerification.add("Verification2");
        String result = "SUCCESS";
        ResponseError error = null; // You can create a sample ResponseError object if needed
        Map<String, Object> event = eventGenerator.getIdentityVerifyEvent(request, result, error);
        assertNotNull(event);
    }

    @Test
    void testGetApplicantGetInfoEvent() {
        Map<String, Object> request = new HashMap<>();
        request.put("email", "user@example.com");
        request.put("mobile", "1234567890");
        String applicantCode = "A123";
        String verifierCode = "V456";
        Map<String, Object> response = new HashMap<>();
        response.put("key1", "value1");
        response.put("key2", "value2");
        int status = 200;
        Map<String, Object> event = eventGenerator.getApplicantGetInfoEvent(request, applicantCode, verifierCode, response, status);
        assertNotNull(event);
    }

    @Test
    void testGetOnboardUpdateEvent() {
        String email = "user@example.com";
        boolean emailOtpVerified = true;
        boolean phoneOtpVerified = false;
        String identityStatus = "PENDING";
        Map<String, Object> event = eventGenerator.getOnboardUpdateEvent(email, emailOtpVerified, phoneOtpVerified, identityStatus);
        assertNotNull(event);
    }


    @Test
    void testGetManualIdentityVerifyEvent() {
        String code = "C123";
        String status = "VERIFIED";
        Map<String, Object> event = eventGenerator.getManualIdentityVerifyEvent(code, status);
        assertNotNull(event);
    }

    @Test
    void testGetOnboardErrorEvent() {
        String email = "user@example.com";
        String action = "ERROR_ACTION";
        ResponseError error = new ResponseError();
        error.setCode( INTERNAL_SERVER_ERROR);
        error.setMessage("error");
        error.setTrace(new Throwable());
        Map<String, Object> event = eventGenerator.getOnboardErrorEvent(email, action, error);
        assertNotNull(event);
    }

    @Test
    void testGetOnboardUserInvite() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("INVITED_BY", "JohnDoe");
        requestBody.put("EMAIL", "user@example.com");
        requestBody.put("ROLE", "Admin");
        String invitedOrgName = "OrgXYZ";
        Map<String, Object> event = eventGenerator.getOnboardUserInvite(requestBody, invitedOrgName);
        assertNotNull(event);
    }

    @Test
    void testGetOnboardUserInviteAccepted() {
        User user = new User("JohnDoe", "123", "Admin", "john@example.com");
        Map<String, Object> participantDetails = new HashMap<>();
        participantDetails.put("PARTICIPANT_NAME", "OrgXYZ");
        Map<String, Object> event = eventGenerator.getOnboardUserInviteAccepted(user, participantDetails);
        assertNotNull(event);
    }

    @Test
    void testGetOnboardUserInviteRejected() {
        User user = new User("JohnDoe", "123", "Admin", "john@example.com");
        String participantName = "OrgXYZ";
        Map<String, Object> event = eventGenerator.getOnboardUserInviteRejected(user, participantName);
        assertNotNull(event);
    }

    @Test
    void testGetSMSMessageEvent() throws JsonProcessingException {
        String message = "Hello, world!";
        List<String> recipients = List.of("1234567890", "9876543210");
        String eventString = eventGenerator.getSMSMessageEvent(message, recipients);
        assertNotNull(eventString);
        assertFalse(eventString.isEmpty());
    }


    @Test
    void testGetEmailMessageEvent() throws JsonProcessingException {
        String message = "Hello, world!";
        String subject = "Test Email";
        List<String> to = List.of("user1@example.com", "user2@example.com");
        List<String> cc = List.of("cc1@example.com", "cc2@example.com");
        List<String> bcc = List.of("bcc1@example.com", "bcc2@example.com");
        String eventString = eventGenerator.getEmailMessageEvent(message, subject, to, cc, bcc);
        assertNotNull(eventString);
        assertFalse(eventString.isEmpty());
    }
}
