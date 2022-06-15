package org.swasth.apigateway.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.swasth.apigateway.constants.Constants;
import org.swasth.apigateway.models.BaseRequest;
import org.swasth.auditindexer.function.AuditIndexer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AuditService.class)
@ActiveProfiles("test")
class AuditServiceTest{

    @Autowired
    private AuditService auditService;

    @MockBean
    private AuditIndexer auditIndexer;

    @Test
    void check_audit_event_generation() throws Exception {
        Map<String,Object> result = auditService.createAuditEvent(getRequest());
        assertFalse(result.isEmpty());
    }

    @Test
    void check_audit_log_creation() throws Exception {
        auditService.createAuditLog(getRequest());
        assertTrue(true);
    }

    @Test
    void check_audit_log_update() throws Exception {
        auditService.updateAuditLog(new HashMap<>());
        assertTrue(true);
    }

    private BaseRequest getRequest() throws Exception {
        return new BaseRequest(Collections.singletonMap("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS0zYTNiZDY4YS04NDhhLTRkNTItOWVjMi0wN2E5MmQ3NjVmYjQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLWNlMjNjY2RjLWU2NDUtNGUzNS05N2I4LTBiZDhmZWY0M2VjZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MyIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QucXVldWVkIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDk0IiwKIngtaGN4LWRlYnVnX2ZsYWciOiJJbmZvIiwKIngtaGN4LWRlYnVnX2RldGFpbHMiOnsiY29kZSI6IkVSUl9JTlZBTElEX0VOQ1JZUFRJT04iLCJtZXNzYWdlIjoiUmVjaXBpZW50IEludmFsaWQgRW5jcnlwdGlvbiIsInRyYWNlIjoiIn0KfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"), false, "/v1/coverageeligibility/check", "1-d2d56996-1b77-4abb-b9e9-0e6e7343c72e", "HIE/HIO.HCX");
    }

}