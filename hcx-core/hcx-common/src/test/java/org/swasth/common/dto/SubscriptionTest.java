package org.swasth.common.dto;

import org.junit.Test;
import org.mockito.Mockito;
import org.swasth.common.utils.Constants;

import static org.junit.Assert.*;

public class SubscriptionTest {

    @Test
    public void testSubscription() {
        Subscription mockSubscription = Mockito.mock(Subscription.class);
        assertNotNull(mockSubscription);
    }


    @Test
    public void testSubscriptionMockData() {
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
        mockSubscription.setIs_delegated(false);
        mockSubscription.setSender_code("test-sender-code");
        mockSubscription.setExpiry(123456789l);
        mockSubscription.setRecipient_code("test-recipient-code");
        assertEquals("hcx-notification-001:hcx-apollo-12345", mockSubscription.getSubscription_id());
        assertEquals("hcx-notification-001",mockSubscription.getTopic_code());
        assertEquals(Constants.ACTIVE_CODE,mockSubscription.getSubscription_status());
        assertFalse(mockSubscription.isIs_delegated());
        assertEquals("test-sender-code",mockSubscription.getSender_code());
        assertEquals(123456789l,mockSubscription.getExpiry());
        assertEquals("test-recipient-code",mockSubscription.getRecipient_code());
    }

    @Test
    public void testSubscriptionConstructor() {
        Subscription mockSubscription = new Subscription("hcx-notification-001:hcx-apollo-12345","sub-request-id","hcx-notification-001",Constants.ACTIVE_CODE,
                "test-sender-code","test-recipient-code",123456789l,false);
        assertEquals("hcx-notification-001:hcx-apollo-12345", mockSubscription.getSubscription_id());
        assertEquals("hcx-notification-001",mockSubscription.getTopic_code());
        assertEquals(Constants.ACTIVE_CODE,mockSubscription.getSubscription_status());
        assertFalse(mockSubscription.isIs_delegated());
        assertEquals("test-sender-code",mockSubscription.getSender_code());
        assertEquals(123456789l,mockSubscription.getExpiry());
        assertEquals("test-recipient-code",mockSubscription.getRecipient_code());
        assertEquals("sub-request-id",mockSubscription.getSubscription_request_id());
    }
}
