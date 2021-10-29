package org.swasth.hcx.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.swasth.hcx.Helpers.KafkaEventGenerator;
import org.swasth.hcx.middleware.KafkaClient;

@WebMvcTest(BaseController.class)
public class BaseControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected KafkaEventGenerator mockKafkaEventGenerator;

    @MockBean
    protected KafkaClient mockKafkaClient;

    @Mock
    protected Environment mockEnv;

    public HttpHeaders getHeaders(){
        HttpHeaders header = new HttpHeaders();
        header.add("alg","RSA-OAEP");
        header.add("enc","A256GCM");
        header.add("x-hcx-sender_code","12345");
        header.add("x-hcx-recipient_code","67890");
        header.add("x-hcx-request_id","req-123");
        header.add("x-hcx-correlation_id","msg-123");
        header.add("x-hcx-timestamp", "2021-10-27T20:35:52.636+0530");
        header.add("x-hcx-status","request.initiate");
        return header;
    }

    public String getRequestBody() throws JsonProcessingException, JSONException {
        JSONObject obj = new JSONObject();
        obj.put("request","payload");
        return String.valueOf(obj);
    }
}
