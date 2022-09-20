package org.swasth.common.utils;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class NotificationUtilsTest {

    public NotificationUtilsTest() throws IOException {
        new NotificationUtils("src/main/resources/networkNotifications.yaml","src/main/resources/participantNotifications.yaml","src/main/resources/workflowNotifications.yaml");
    }

    @Test
    public void testLoadNotification() {
        assertTrue(NotificationUtils.notificationList.size() > 1);
        assertTrue(NotificationUtils.topicCodeList.contains("notif-participant-onboarded"));
    }

    @Test
    public void testValidTopicCode() {
        assertTrue(NotificationUtils.isValidCode("notif-participant-onboarded"));
    }

    @Test
    public void testInvalidTopicCode() {
        assertFalse(NotificationUtils.isValidCode("test-123"));
    }

    @Test
    public void testGetNotification() {
        assertEquals(Constants.NETWORK, NotificationUtils.getNotification("notif-participant-onboarded").get(Constants.CATEGORY));
    }
}
