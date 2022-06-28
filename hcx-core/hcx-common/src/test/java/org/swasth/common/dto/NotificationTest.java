package org.swasth.common.dto;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.*;

public class NotificationTest {

    @Test
   public void testNotification() throws Exception {
        Notification mockNotification = Mockito.mock(Notification.class);
        assertNotNull(mockNotification);
    }


    @Test
    public void testNotificationMockData() {
        Notification mockNotification = Mockito.mock(Notification.class);
        assertNull(mockNotification.getCategory());
        assertNull(mockNotification.getDescription());
        assertNull(mockNotification.getStatus());
        assertNull(mockNotification.getTopicCode());
        assertNull(mockNotification.getTitle());
        assertEquals(new HashSet<>(), mockNotification.getAllowedRecipients());
        assertEquals(new HashSet<>(),mockNotification.getAllowedSenders());
        assertNull(mockNotification.getTemplate());
        assertNull(mockNotification.getTrigger());
        assertNull(mockNotification.getType());
    }

    @Test
    public void testNotificationData() {
        Notification notification = new Notification();
        notification.setCategory("Category");
        notification.setDescription("Description");
        notification.setTopicCode("Id");
        notification.setTitle("Name");
        notification.setAllowedRecipients(null);
        notification.setAllowedSenders(null);
        notification.setTemplate("Template");
        notification.setTrigger("Trigger");
        notification.setType("Type");
        notification.setStatus("Status");

        assertEquals("Category", notification.getCategory());
        assertEquals("Description",notification.getDescription());
        assertEquals("Status",notification.getStatus());
        assertEquals("Id",notification.getTopicCode());
        assertEquals("Name",notification.getTitle());
        assertNull(notification.getAllowedRecipients());
        assertNull(notification.getAllowedSenders());
        assertEquals("Template", notification.getTemplate());
        assertEquals("Trigger",notification.getTrigger());
        assertEquals("Type",notification.getType());
    }


}
