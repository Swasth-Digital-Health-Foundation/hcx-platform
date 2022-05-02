package org.swasth.common.dto;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NotificationTest {

    @Test
   public void testNotification() throws Exception {
        Notification mockNotification = Mockito.mock(Notification.class);
        assertNotNull(mockNotification);
    }


    @Test
    public void testNotificationMockData() throws Exception {
        Notification mockNotification = Mockito.mock(Notification.class);
        assertEquals(null, mockNotification.getCategory());
        assertEquals(null,mockNotification.getDescription());
        assertEquals(new ArrayList<>(), mockNotification.getEventType());
        assertEquals(null,mockNotification.getStatus());
        assertEquals(null, mockNotification.getId());
        assertEquals(null,mockNotification.getName());
        assertEquals(new ArrayList<>(), mockNotification.getRecipient());
        assertEquals(new ArrayList<>(),mockNotification.getSender());
        assertEquals(null, mockNotification.getTemplate());
        assertEquals(null,mockNotification.getTrigger());
        assertEquals(null,mockNotification.getType());
    }

    @Test
    public void testNotificationData() throws Exception {
        Notification notification = new Notification();
        notification.setCategory("Category");
        notification.setDescription("Description");
        notification.setEventType(null);
        notification.setId(UUID.randomUUID());
        notification.setName("Name");
        notification.setRecipient(null);
        notification.setSender(null);
        notification.setTemplate("Template");
        notification.setTrigger("Trigger");
        notification.setType("Type");
        notification.setStatus("Status");

        assertEquals("Category", notification.getCategory());
        assertEquals("Description",notification.getDescription());
        assertEquals(null, notification.getEventType());
        assertEquals("Status",notification.getStatus());
        assertNotNull(notification.getId());
        assertEquals("Name",notification.getName());
        assertEquals(null, notification.getRecipient());
        assertEquals(null,notification.getSender());
        assertEquals("Template", notification.getTemplate());
        assertEquals("Trigger",notification.getTrigger());
        assertEquals("Type",notification.getType());
    }


}
