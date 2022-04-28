package org.swasth.common.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.swasth.common.utils.Constants.ACTIVE;
import static org.swasth.common.utils.Constants.IN_ACTIVE;

public class ResponseTest {

    @Test
    public void testSubscription(){
        Response response = new Response();
        response.setSubscriptionStatus(ACTIVE);
        response.setSubscriptionId("hcx-notification-001:hcx-apollo-12345");
        assertEquals(ACTIVE,response.getSubscriptionStatus());
        assertEquals("hcx-notification-001:hcx-apollo-12345", response.getSubscriptionId());
    }

    @Test
    public void testSubscriptionList(){
        Response response = new Response();
        List<Subscription> subscriptionList = getSubscriptionList();
        response.setSubscriptions(subscriptionList);
        response.setSubscriptionCount(subscriptionList.size());
        assertEquals(2,response.getSubscriptions().size());
        assertEquals(2,response.getSubscriptionCount());
        assertEquals(ACTIVE, response.getSubscriptions().get(0).getStatus());
        assertEquals(IN_ACTIVE, response.getSubscriptions().get(1).getStatus());
    }

    private List<Subscription> getSubscriptionList()  {
        List<Subscription> subscriptionList = new ArrayList<>();
        Subscription subscription = new Subscription();
        subscription.setSubscriptionId("hcx-notification-001:hcx-apollo-12345");
        subscription.setStatus(ACTIVE);
        subscription.setNotificationId("hcx-notification-001");

        Subscription inActiveSubscription = new Subscription();
        inActiveSubscription.setSubscriptionId("hcx-notification-002:hcx-apollo-12345");
        inActiveSubscription.setStatus(IN_ACTIVE);
        inActiveSubscription.setNotificationId("hcx-notification-002");

        subscriptionList.add(subscription);
        subscriptionList.add(inActiveSubscription);
        return subscriptionList;
    }
}
