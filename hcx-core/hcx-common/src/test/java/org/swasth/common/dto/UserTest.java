package org.swasth.common.dto;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class UserTest {

    private User user;


    @Before
    public void setUp() {
        user = new User("test-123", "test-123@yopmail.com", "9620499129", "test-123@swasth-hcx");
    }

    @Test
    public void testGetUserDetails() {
        String userName = "test-123";
        user.setUserName(userName);
        String email = "test-123@yopmail.com";
        user.setEmail(email);
        String mobile = "9620499129";
        user.setMobile(mobile);
        String createdBy = "test-123@swasth-hcx";
        user.setCreatedBy(createdBy);
        assertEquals(email, user.getEmail());
        assertEquals(userName, user.getUsername());
        assertEquals(mobile, user.getMobile());
        assertEquals(createdBy, user.getCreatedBy());
    }

    @Test
    public void testAddTenantRole() {
        String participantCode = "test-123@swasth-hcx";
        String role = "admin";
        user.addTenantRole(participantCode, role);
        List<Map<String, Object>> tenantRoles = user.getTenantRoles();
        Map<String, Object> tenantRole = tenantRoles.get(0);
        user.setTenantRoles(tenantRoles);
        assertNotNull(tenantRoles);
        assertEquals(participantCode, tenantRole.get("participant_code"));
        assertEquals(role, tenantRole.get("role"));
    }

    @Test
    public void testEmptyConstructor() {
        User user = new User();
        user.setUserId(null);
        assertNull(user.getUserId());
    }
}