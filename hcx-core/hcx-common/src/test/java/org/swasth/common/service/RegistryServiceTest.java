package org.swasth.common.service;

import org.junit.Test;
import org.swasth.common.exception.ServerException;


public class RegistryServiceTest {

    @Test(expected = ServerException.class)
    public void test_get_details_server_exception() throws Exception {
        RegistryService service = new RegistryService("localhost:8081");
        service.getDetails("{ \"filters\": { } }");
    }
}
