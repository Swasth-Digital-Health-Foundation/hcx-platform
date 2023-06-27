package org.swasth.common.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.*;

public class RegistryResponseTest {

    @Test
    public void testWithOrganizationEntity() {
        String entity = "Organisation";
        String value = "test-user@swasth-hcx";
        RegistryResponse response = new RegistryResponse(value, entity);
        response.setParticipantCode("test-user@swasth-hcx");
        response.setTimestamp(1234567890L);
        response.setStatus("active");
        assertEquals(value, response.getParticipantCode());
        assertEquals(Long.valueOf(1234567890), response.getTimestamp());
        assertEquals("active", response.getStatus());
    }

    @Test
    public void testWithUserEntity() {
        String entity = "User";
        String value = "test-user@swasth-hcx";
        RegistryResponse response = new RegistryResponse(value, entity);
        response.setParticipantCode("test-user@swasth-hcx");
        assertEquals(value, response.getUserId());
    }

    @Test
    public void testWithUserArraylistOrgnisationEntity() {
        String entity = "Organisation";
        ArrayList<Object> value = new ArrayList<>();
        value.add(Map.of("user_id", "test-user@swasth-hcx", "user_name", "test-user"));
        RegistryResponse response = new RegistryResponse(value, entity);
        assertEquals(value, response.getParticipants());
    }

    @Test
    public void testWithUserArraylistUserEntity() {
        String entity = "User";
        ArrayList<Object> value = new ArrayList<>();
        value.add(Map.of("user_id", "test-user@swasth-hcx", "user_name", "test-user"));
        RegistryResponse response = new RegistryResponse(value, entity);
        assertEquals(value, response.getUsers());
    }

    @Test
    public void testAddRemoveResponse(){
        long timestamp = System.currentTimeMillis();
        String status = "SUCCESS";
        RegistryResponse response = new RegistryResponse(timestamp,status);
        assertEquals(status,response.getStatus());
    }

    @Test
    public void testTimestap(){
        RegistryResponse response = new RegistryResponse();
        assertNotNull(response.getTimestamp());
    }
}
