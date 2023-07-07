package org.swasth.hcx.controllers.v1;

import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.swasth.hcx.controllers.BaseSpec;

import java.io.IOException;
import java.net.InetAddress;

class OnboardControllerTests extends BaseSpec{

    private final MockWebServer registryServer =  new MockWebServer();

    @BeforeEach
    public void start() throws IOException {
        registryServer.start(InetAddress.getByName("localhost"),8082);
    }

    @AfterEach
    public void teardown() throws IOException, InterruptedException {
        registryServer.shutdown();
        Thread.sleep(2000);
    }

}
