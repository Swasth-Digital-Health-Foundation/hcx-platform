package org.swasth.common.dto;

import org.junit.Test;
import org.mockito.Mockito;
import org.swasth.common.utils.Constants;

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
    public void testSubscriptionData() {
        Subscription mockSubscription = new Subscription("hcx-notification-001:hcx-apollo-12345", "hcx-notification-001", Constants.ACTIVE, "API");
        assertEquals("hcx-notification-001:hcx-apollo-12345", mockSubscription.getSubscriptionId());
        assertEquals("hcx-notification-001",mockSubscription.getNotificationId());
        assertEquals(Constants.ACTIVE,mockSubscription.getStatus());
        assertEquals("API",mockSubscription.getMode());
    }
}
