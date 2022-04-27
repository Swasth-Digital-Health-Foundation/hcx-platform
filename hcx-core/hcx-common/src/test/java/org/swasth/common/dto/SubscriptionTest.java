package org.swasth.common.dto;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

public class SubscriptionTest {

    @Test
    public void testSubscription() throws Exception {
        Subscription mockSubscription = Mockito.mock(Subscription.class);
        assertNotNull(mockSubscription);
    }


    @Test
    public void testSubscriptionData() throws Exception {
        Subscription mockSubscription = Mockito.mock(Subscription.class);
        doReturn("SubscriptionId").when(mockSubscription).getSubscriptionId();
        doReturn("NotificationId").when(mockSubscription).getNotificationId();
        doReturn("Status").when(mockSubscription).getStatus();
        assertEquals("SubscriptionId", mockSubscription.getSubscriptionId());
        assertEquals("NotificationId",mockSubscription.getNotificationId());
        assertEquals("Status",mockSubscription.getStatus());
    }
}
