package org.swasth.common.dto;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

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
        assertEquals(new ArrayList<>(), mockNotification.getEventType());
        assertNull(mockNotification.getStatus());
        assertNull(mockNotification.getId());
        assertNull(mockNotification.getName());
        assertEquals(new ArrayList<>(), mockNotification.getRecipient());
        assertEquals(new ArrayList<>(),mockNotification.getSender());
        assertNull(mockNotification.getTemplate());
        assertNull(mockNotification.getTrigger());
        assertNull(mockNotification.getType());
    }

    @Test
    public void testNotificationData() throws Exception {
        Notification notification = new Notification();
        notification.setCategory("Category");
        notification.setDescription("Description");
        notification.setEventType(null);
        notification.setId("Id");
        notification.setName("Name");
        notification.setRecipient(null);
        notification.setSender(null);
        notification.setTemplate("Template");
        notification.setTrigger("Trigger");
        notification.setType("Type");
        notification.setStatus("Status");

        assertEquals("Category", notification.getCategory());
        assertEquals("Description",notification.getDescription());
        assertNull(notification.getEventType());
        assertEquals("Status",notification.getStatus());
        assertEquals("Id",notification.getId());
        assertEquals("Name",notification.getName());
        assertNull(notification.getRecipient());
        assertNull(notification.getSender());
        assertEquals("Template", notification.getTemplate());
        assertEquals("Trigger",notification.getTrigger());
        assertEquals("Type",notification.getType());
    }


}
