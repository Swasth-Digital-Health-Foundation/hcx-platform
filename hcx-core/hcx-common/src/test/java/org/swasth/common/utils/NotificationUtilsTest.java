package org.swasth.common.utils;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class NotificationUtilsTest {

    public NotificationUtilsTest() throws IOException {
        new NotificationUtils();
    }

    @Test
    public void testLoadNotification() {
        assertEquals(3, NotificationUtils.notificationList.size());
        assertTrue(NotificationUtils.topicCodeList.contains("24e975d1-054d-45fa-968e-c91b1043d0a5"));
    }

    @Test
    public void testValidTopicCode() {
        assertTrue(NotificationUtils.isValidCode("24e975d1-054d-45fa-968e-c91b1043d0a5"));
    }

    @Test
    public void testInvalidTopicCode() {
        assertFalse(NotificationUtils.isValidCode("test-123"));
    }

    @Test
    public void testGetNotification() {
        assertEquals(Constants.NETWORK, NotificationUtils.getNotification("24e975d1-054d-45fa-968e-c91b1043d0a5").get(Constants.CATEGORY));
    }
}
