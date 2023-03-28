package org.swasth.apigateway.filters;

import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.swasth.apigateway.BaseSpec;
import org.swasth.common.utils.Constants;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OnboardRequestTest extends BaseSpec {


    @Test
    public void onboard_request_success_scenario() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(202)
                .addHeader("Content-Type", "application/json"));

        client.post().uri(versionPrefix + Constants.PARTICIPANT_ONBOARD_UPDATE)
                .header(Constants.AUTHORIZATION, getUpdateUserToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(getOnboardUpdateRequest())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> assertEquals(HttpStatus.ACCEPTED, result.getStatus()));
    }
}

