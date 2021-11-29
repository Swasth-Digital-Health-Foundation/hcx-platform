package org.swasth.hcx.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = {KafkaEventGenerator.class})
public class KafkaEventGeneratorTests {

    @Autowired
    KafkaEventGenerator kafkaEventGenerator;

    @Test
    public void check_generatePayloadEvent() throws JsonProcessingException {
        String result = kafkaEventGenerator.generatePayloadEvent("test_123", new HashMap<>());
        assert (!result.isEmpty());
    }

    @Test
    public void check_generateMetadataEvent() throws Exception {
        String result = kafkaEventGenerator.generateMetadataEvent("test", "/test", getRequestBody());
        assert (!result.isEmpty());
    }

    public Map<String, Object> getRequestBody() throws JsonProcessingException {
    Map<String, Object> obj = new HashMap<>();
    obj.put("protected","eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwKImFsZyI6IkEyNTZHQ00iLAoieC1oY3gtc2VuZGVyX2NvZGUiOiIxMjM0NSIsCiJ4LWhjeC1yZWNpcGllbnRfY29kZSI6IjY3ODkwIiwKIngtaGN4LXJlcXVlc3RfaWQiOiJyZXEtMTIzIiwKIngtaGN4LWNvcnJlbGF0aW9uX2lkIjoibXNnLTEyMyIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoiandzX2hlYWRlciI6eyJ0eXAiOiJKV1QiLCAiYWxnIjoiUlMyNTYifSwKImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn0sCiJ1c2VfY2FzZV9uYW1lIjoidGVzdCIsCiJwYXJhbWV0ZXJfbmFtZSI6InRlc3QiCn0=");
    obj.put("encrypted_key","6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ");
    obj.put("aad","eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");
    obj.put("iv","AxY8DCtDaGlsbGljb3RoZQ");
    obj.put("ciphertext","KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY");
    obj.put("tag","Mz-VPPyU4RlcuYv1IwIvzw");
    return obj;
    }

}