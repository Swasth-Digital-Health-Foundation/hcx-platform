package org.swasth.common.dto;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SubscriptionTest {

    @Test
    public void testSubscription() throws Exception {
        Subscription mockSubscription = Mockito.mock(Subscription.class);
        assertNotNull(mockSubscription);
    }


    @Test
    public void testSubscriptionMockData() throws Exception {
        Subscription mockSubscription = Mockito.mock(Subscription.class);
        assertEquals(null, mockSubscription.getSubscriptionId());
        assertEquals(null,mockSubscription.getNotificationId());
        assertEquals(null,mockSubscription.getStatus());
        assertEquals(null,mockSubscription.getMode());
    }

    @Test
    public void testSubscriptionData() throws Exception {
        Subscription mockSubscription = new Subscription();
        mockSubscription.setSubscriptionId("SubscriptionId");
        mockSubscription.setNotificationId("NotificationId");
        mockSubscription.setStatus("Status");
        mockSubscription.setMode("API");
        assertEquals("SubscriptionId", mockSubscription.getSubscriptionId());
        assertEquals("NotificationId",mockSubscription.getNotificationId());
        assertEquals("Status",mockSubscription.getStatus());
        assertEquals("API",mockSubscription.getMode());
    }
}
