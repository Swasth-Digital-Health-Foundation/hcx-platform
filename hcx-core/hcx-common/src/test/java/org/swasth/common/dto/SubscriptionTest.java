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
        //subscription_id,subscription_status,topic_code,sender_code,recipient_code,expiry,is_delegated
        Subscription mockSubscription = Mockito.mock(Subscription.class);
        assertEquals(null, mockSubscription.getSubscription_id());
        assertEquals(0,mockSubscription.getSubscription_status());
        assertEquals(null,mockSubscription.getTopic_code());
        assertEquals(null,mockSubscription.getSender_code());
        assertEquals(null,mockSubscription.getRecipient_code());
        assertEquals(0l,mockSubscription.getExpiry());
        assertEquals(false,mockSubscription.isIs_delegated());
    }

    @Test
    public void testSubscriptionData() {
        Subscription mockSubscription = new Subscription();
        mockSubscription.setSubscription_id("hcx-notification-001:hcx-apollo-12345");
        mockSubscription.setTopic_code("hcx-notification-001");
        mockSubscription.setSubscription_status(Constants.ACTIVE_CODE);
        assertEquals("hcx-notification-001:hcx-apollo-12345", mockSubscription.getSubscription_id());
        assertEquals("hcx-notification-001",mockSubscription.getTopic_code());
        assertEquals(Constants.ACTIVE_CODE,mockSubscription.getSubscription_status());
    }
}
