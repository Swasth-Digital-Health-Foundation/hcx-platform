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
import org.swasth.common.JsonUtils;
import org.swasth.common.dto.Response;
import org.swasth.hcx.helpers.EventGenerator;
import org.swasth.hcx.managers.HealthCheckManager;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;

import java.util.HashMap;
import java.util.Map;

@WebMvcTest()
@ExtendWith(MockitoExtension.class)
public class BaseSpec {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected EventGenerator mockEventGenerator;

    @Mock
    protected Environment mockEnv;

    @MockBean
    protected IEventService mockKafkaClient;

    @MockBean
    protected IDatabaseService postgreSQLClient;

    @MockBean
    protected HealthCheckManager healthCheckManager;

    public String getRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMGM5MThhYTEtM2E1NC00ZTU2LTgwZGMtMWQ0NjQzNzcxYTdkIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiZjgyZDAxZjktYjMzNC00ZDBiLTk5MzMtNWZhZTdhMmMzY2I1IiwKIngtaGN4LXJlcXVlc3RfaWQiOiIyNmIxMDYwYy0xZTgzLTQ2MDAtOTYxMi1lYTMxZTBjYTUwOTEiLAoieC1oY3gtY29ycmVsYXRpb25faWQiOiI1ZTkzNGY5MC0xMTFkLTRmMGItYjAxNi1jMjJkODIwNjc0ZTEiLAoieC1oY3gtdGltZXN0YW1wIjoiMjAyMS0xMC0yN1QyMDozNTo1Mi42MzYrMDUzMCIsCiJ4LWhjeC1zdGF0dXMiOiJyZXF1ZXN0LmluaXRpYXRlIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMWU4My00NjAwLTk2MTIiLAoieC1oY3gtZGVidWdfZmxhZyI6IkluZm8iLAoieC1oY3gtZXJyb3JfZGV0YWlscyI6eyJlcnJvci5jb2RlIjogImJhZC5pbnB1dCIsICJlcnJvci5tZXNzYWdlIjogIlByb3ZpZGVyIGNvZGUgbm90IGZvdW5kIiwgInRyYWNlIjogIiJ9LAoieC1oY3gtZGVidWdfZGV0YWlscyI6eyJlcnJvci5jb2RlIjogImJhZC5pbnB1dCIsICJlcnJvci5tZXNzYWdlIjogIlByb3ZpZGVyIGNvZGUgbm90IGZvdW5kIiwidHJhY2UiOiIifSwKImp3c19oZWFkZXIiOnsidHlwIjoiSldUIiwgImFsZyI6IlJTMjU2In0sCiJqd2VfaGVhZGVyIjp7ImFsZyI6IlJTQS1PQUVQIiwiZW5jIjoiQTI1NkdDTSJ9Cn0=.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JsonUtils.serialize(obj);
    }

    public String getBadRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwKImFsZyI6IkEyNTZHQ00iLAoieC1oY3gtc2VuZGVyX2NvZGUiOiIxMjM0NSIsCiJ4LWhjeC1yZWNpcGllbnRfY29kZSI6IjY3ODkwIiwKIngtaGN4LXJlcXVlc3RfaWQiOiJyZXEtMTIzIiwKIngtaGN4LWNvcnJlbGF0aW9uX2lkIjoibXNnLTEyMyIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoiandzX2hlYWRlciI6eyJ0eXAiOiJKV1QiLCAiYWxnIjoiUlMyNTYifSwKImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn0sCiJ1c2VfY2FzZV9uYW1lIjoidGVzdCIsCiJwYXJhbWV0ZXJfbmFtZSI6InRlc3QiCn0=.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.");
        return JsonUtils.serialize(obj);
    }

    public String getHeadersMissingRequestBody() throws JsonProcessingException {
      Map<String,Object> obj = new HashMap<>();
      obj.put("payload","eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwKImFsZyI6IkEyNTZHQ00iLAoieC1oY3gtc2VuZGVyX2NvZGUiOiIxMjM0NSIsCiJ4LWhjeC1yZWNpcGllbnRfY29kZSI6IjY3ODkwIiwKIngtaGN4LXJlcXVlc3RfaWQiOiIyNmIxMDYwYy0xZTgzLTQ2MDAtOTYxMi1lYTMxZTBjYTUwOTEiLAoieC1oY3gtY29ycmVsYXRpb25faWQiOiI1ZTkzNGY5MC0xMTFkLTRmMGItYjAxNi1jMjJkODIwNjc0ZTEiLAoieC1oY3gtdGltZXN0YW1wIjoiMjAyMS0xMC0yN1QyMDozNTo1Mi42MzYrMDUzMCIsCiJqd3NfaGVhZGVyIjp7InR5cCI6IkpXVCIsICJhbGciOiJSUzI1NiJ9LAoiandlX2hlYWRlciI6eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifSwKInVzZV9jYXNlX25hbWUiOiJ0ZXN0IiwKInBhcmFtZXRlcl9uYW1lIjoidGVzdCIKfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JsonUtils.serialize(obj);
    }

    public Response validHealthResponse() {
        return new Response("healthy",true);
    }
}
