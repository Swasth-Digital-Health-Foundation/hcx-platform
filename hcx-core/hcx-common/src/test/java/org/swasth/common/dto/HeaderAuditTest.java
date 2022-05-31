package org.swasth.common.dto;

import org.junit.Test;
import org.swasth.common.utils.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class HeaderAuditTest {

    @Test
    public void check_header_audit_dto() {
        HeaderAudit audit = new HeaderAudit("AUDIT", new Object(), new Object(), "1-2799b6a4-cf2d-45fe-a5e1-5f1c82979e0d", "93f908ba", "26b1060c-1e83-4600-9612-ea31e0ca5091", "1e83-460a-4f0b-b016-c22d820674e1", "5e934f90-111d-4f0b-b016-c22d820674e1", "2022-01-06T09:50:23+00", new Long("1642781095099"), new Long("1642781095099"), new Long("1642781095099"),  Constants.COVERAGE_ELIGIBILITY_CHECK, "200c6dac-b259-4d35-b176-370fb092d7b0", "request.dispatched", Arrays.asList("provider"), Arrays.asList("payor"), "test_payload");
        assertEquals("AUDIT", audit.getEid());
    }

    @Test
    public void check_header_audit_dto_payload_scenario() {
        HeaderAudit audit = new HeaderAudit();
        audit.setPayload("test_payload");
        audit.setNotificationId("notification-123");
        audit.setNotificationData(Collections.singletonMap("amount", 30000));
        audit.setNotificationDispatchResult(Collections.singletonMap("recipient-123", "success"));
        assertEquals("test_payload", audit.getPayload());
        assertEquals("notification-123", audit.getNotificationId());
        assertFalse(((Map<String, Object>) audit.getNotificationData()).isEmpty());
        assertFalse(((Map<String, Object>) audit.getNotificationDispatchResult()).isEmpty());
    }

}
