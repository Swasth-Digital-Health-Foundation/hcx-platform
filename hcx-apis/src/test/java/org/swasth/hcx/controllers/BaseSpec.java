package org.swasth.hcx.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.swasth.common.StringUtils;
import org.swasth.hcx.helpers.KafkaEventGenerator;
import org.swasth.kafka.client.KafkaClient;

import java.util.HashMap;
import java.util.Map;

@WebMvcTest()
@ExtendWith(MockitoExtension.class)
public class BaseSpec {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected KafkaEventGenerator mockKafkaEventGenerator;

    @Mock
    protected Environment mockEnv;

    @MockBean
    protected KafkaClient mockKafkaClient;

    public String getRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("protected","eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwKImFsZyI6IkEyNTZHQ00iLAoieC1oY3gtc2VuZGVyX2NvZGUiOiIxMjM0NSIsCiJ4LWhjeC1yZWNpcGllbnRfY29kZSI6IjY3ODkwIiwKIngtaGN4LXJlcXVlc3RfaWQiOiJyZXEtMTIzIiwKIngtaGN4LWNvcnJlbGF0aW9uX2lkIjoibXNnLTEyMyIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoiandzX2hlYWRlciI6eyJ0eXAiOiJKV1QiLCAiYWxnIjoiUlMyNTYifSwKImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn0sCiJ1c2VfY2FzZV9uYW1lIjoidGVzdCIsCiJwYXJhbWV0ZXJfbmFtZSI6InRlc3QiCn0=");
        obj.put("encrypted_key","6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ");
        obj.put("aad","eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");
        obj.put("iv","AxY8DCtDaGlsbGljb3RoZQ");
        obj.put("ciphertext","KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY");
        obj.put("tag","Mz-VPPyU4RlcuYv1IwIvzw");
        return StringUtils.serialize(obj);
    }

    public String getBadRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("protected","eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwKImFsZyI6IkEyNTZHQ00iLAoieC1oY3gtc2VuZGVyX2NvZGUiOiIxMjM0NSIsCiJ4LWhjeC1yZWNpcGllbnRfY29kZSI6IjY3ODkwIiwKIngtaGN4LXJlcXVlc3RfaWQiOiJyZXEtMTIzIiwKIngtaGN4LWNvcnJlbGF0aW9uX2lkIjoibXNnLTEyMyIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoiandzX2hlYWRlciI6eyJ0eXAiOiJKV1QiLCAiYWxnIjoiUlMyNTYifSwKImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn0sCiJ1c2VfY2FzZV9uYW1lIjoidGVzdCIsCiJwYXJhbWV0ZXJfbmFtZSI6InRlc3QiCn0=");
        obj.put("encrypted_key","6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ");
        obj.put("aad","eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");
        obj.put("iv","AxY8DCtDaGlsbGljb3RoZQ");
        obj.put("ciphertext","KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY");
        return StringUtils.serialize(obj);
    }

    public String getHeadersMissingRequestBody() throws JsonProcessingException {
      Map<String,Object> obj = new HashMap<>();
      obj.put("protected","eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwKImFsZyI6IkEyNTZHQ00iLAoieC1oY3gtc2VuZGVyX2NvZGUiOiIxMjM0NSIsCiJ4LWhjeC1yZWNpcGllbnRfY29kZSI6IjY3ODkwIiwKIngtaGN4LXJlcXVlc3RfaWQiOiIyNmIxMDYwYy0xZTgzLTQ2MDAtOTYxMi1lYTMxZTBjYTUwOTEiLAoieC1oY3gtY29ycmVsYXRpb25faWQiOiI1ZTkzNGY5MC0xMTFkLTRmMGItYjAxNi1jMjJkODIwNjc0ZTEiLAoieC1oY3gtdGltZXN0YW1wIjoiMjAyMS0xMC0yN1QyMDozNTo1Mi42MzYrMDUzMCIsCiJqd3NfaGVhZGVyIjp7InR5cCI6IkpXVCIsICJhbGciOiJSUzI1NiJ9LAoiandlX2hlYWRlciI6eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifSwKInVzZV9jYXNlX25hbWUiOiJ0ZXN0IiwKInBhcmFtZXRlcl9uYW1lIjoidGVzdCIKfQ==");
        obj.put("encrypted_key","6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ");
        obj.put("aad","eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");
        obj.put("iv","AxY8DCtDaGlsbGljb3RoZQ");
        obj.put("ciphertext","KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY");
        obj.put("tag","Mz-VPPyU4RlcuYv1IwIvzw");
        return StringUtils.serialize(obj);
    }
}
