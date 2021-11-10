package org.swasth.hcx.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.swasth.hcx.helpers.KafkaEventGenerator;
import org.swasth.kafka.client.KafkaClient;

@WebMvcTest()
@ExtendWith(MockitoExtension.class)
public class BaseSpec {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected KafkaEventGenerator mockKafkaEventGenerator;

    @Mock
    protected Environment mockEnv;

    protected KafkaClient mockKafkaClient = Mockito.mock(KafkaClient.class);

    public String getRequestBody() throws JsonProcessingException, JSONException {
        JSONObject obj = new JSONObject();
        obj.put("protected","eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwKImFsZyI6IkEyNTZHQ00iLAoieC1oY3gtc2VuZGVyX2NvZGUiOiIxMjM0NSIsCiJ4LWhjeC1yZWNpcGllbnRfY29kZSI6IjY3ODkwIiwKIngtaGN4LXJlcXVlc3RfaWQiOiJyZXEtMTIzIiwKIngtaGN4LWNvcnJlbGF0aW9uX2lkIjoibXNnLTEyMyIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoiandzX2hlYWRlciI6eyJ0eXAiOiJKV1QiLCAiYWxnIjoiUlMyNTYifSwKImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn0sCiJ1c2VfY2FzZV9uYW1lIjoidGVzdCIsCiJwYXJhbWV0ZXJfbmFtZSI6InRlc3QiCn0=");
        obj.put("encrypted_key","6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ");
        obj.put("aad","eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");
        obj.put("iv","AxY8DCtDaGlsbGljb3RoZQ");
        obj.put("ciphertext","KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY");
        obj.put("tag","Mz-VPPyU4RlcuYv1IwIvzw");
        return String.valueOf(obj);
    }
}
