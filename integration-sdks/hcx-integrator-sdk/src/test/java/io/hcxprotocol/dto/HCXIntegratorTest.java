package io.hcxprotocol.dto;

import io.hcxprotocol.init.HCXIntegrator;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HCXIntegratorTest {

    @Test
    public void testInitializeConfigMap() throws Exception {
        Map<String,Object> configMap = new HashMap<>();
        configMap.put("protocolBasePath", "http://localhost:8095");
        configMap.put("participantCode", "participant@01");
        configMap.put("authBasePath", "http://localhost:8080");
        configMap.put("username", "participant@gmail.com");
        configMap.put("password", "12345");
        configMap.put("encryptionPrivateKey", "Mz-VPPyU4RlcuYv1IwIvzw");
        configMap.put("igUrl", "http://localhost:8090");

        HCXIntegrator.init(configMap);
        HCXIntegrator hcxIntegrator = HCXIntegrator.getInstance();

        assertEquals("http://localhost:8095", hcxIntegrator.getHCXProtocolBasePath());
        assertEquals("participant@01", hcxIntegrator.getParticipantCode());
        assertEquals("http://localhost:8080", hcxIntegrator.getAuthBasePath());
        assertEquals("participant@gmail.com", hcxIntegrator.getUsername());
        assertEquals("12345", hcxIntegrator.getPassword());
        assertEquals("Mz-VPPyU4RlcuYv1IwIvzw", hcxIntegrator.getPrivateKey());
        assertEquals("http://localhost:8090", hcxIntegrator.getIGUrl());

        configMap.put("password", "67890");
        HCXIntegrator.init(configMap);

        assertEquals("67890", hcxIntegrator.getPassword());
    }

    @Test
    public void testInitializeConfigString() throws Exception {
        String configStr = "{\"password\":\"12345\",\"protocolBasePath\":\"http://localhost:8095\",\"igUrl\":\"http://localhost:8090\",\"authBasePath\":\"http://localhost:8080\",\"encryptionPrivateKey\":\"Mz-VPPyU4RlcuYv1IwIvzw\",\"participantCode\":\"participant@01\",\"username\":\"participant@gmail.com\"}";

        HCXIntegrator.init(configStr);
        HCXIntegrator hcxIntegrator = HCXIntegrator.getInstance();

        assertEquals("http://localhost:8095", hcxIntegrator.getHCXProtocolBasePath());
        assertEquals("participant@01", hcxIntegrator.getParticipantCode());
        assertEquals("http://localhost:8080", hcxIntegrator.getAuthBasePath());
        assertEquals("participant@gmail.com", hcxIntegrator.getUsername());
        assertEquals("12345", hcxIntegrator.getPassword());
        assertEquals("Mz-VPPyU4RlcuYv1IwIvzw", hcxIntegrator.getPrivateKey());
        assertEquals("http://localhost:8090", hcxIntegrator.getIGUrl());
    }

    @Test(expected = Exception.class)
    public void testWithoutConfigVariablesInitialization() throws Exception {
        HCXIntegrator hcxIntegrator = HCXIntegrator.getInstance();
    }
}
