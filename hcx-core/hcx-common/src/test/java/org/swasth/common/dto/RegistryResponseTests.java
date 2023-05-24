package org.swasth.common.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RegistryResponseTests {


    @Test
    public void testConstructorWithOrganizationEntity() {
        String entity = "Organisation";
        String value = "test-user@swasth-hcx";
        RegistryResponse response = new RegistryResponse(value, entity);
        assertEquals(value, response.getParticipantCode());
        assertNull(response.getTimestamp());
    }
}
