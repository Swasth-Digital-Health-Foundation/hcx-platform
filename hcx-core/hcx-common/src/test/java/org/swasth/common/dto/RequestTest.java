package org.swasth.common.dto;

import org.junit.Test;
import org.swasth.common.exception.ClientException;
import org.swasth.common.utils.Constants;

import java.util.HashMap;
import java.util.Map;


public class RequestTest {

    @Test
    public void check_payload() throws Exception {
        Request request = new Request(getRequestBody());
        request.setStatus(Constants.COMPLETE_STATUS);
    }

    @Test
    public void check_plain_payload() throws Exception {
        Request request = new Request(getPlainRequestBody());
    }

    @Test(expected = ClientException.class)
    public void check_exception_payload() throws Exception {
        Request request = new Request(null);
    }

    public Map<String, Object> getRequestBody() {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS05ODc1Ni1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLTI3OTliNmE0LWNmMmQtNDVmZS1hNWUxLTVmMWM4Mjk3OWUwZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1zdGF0dXNfZmlsdGVycyI6eyJyZXF1ZXN0X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MTAxIn0sCiJqd3NfaGVhZGVyIjp7InR5cCI6IkpXVCIsICJhbGciOiJSUzI1NiJ9LAoiandlX2hlYWRlciI6eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return obj;
    }

    public Map<String, Object> getPlainRequestBody() {
        Map<String,Object> obj = new HashMap<>();
        obj.put("x-hcx-status","response.error");
        obj.put("x-hcx-sender_code","1-0756766c-ad43-4145-86ea-d1b17b729a3f");
        obj.put("x-hcx-recipient_code","1-68c5deca-8299-4feb-b441-923bb649a9a3");
        obj.put("x-hcx-correlation_id","5e934f90-111d-4f0b-b016-c22d820674e4");
        obj.put("x-hcx-workflow_id","1e83-460a-4f0b-b016-c22d820674e1");
        obj.put("x-hcx-error_details", new HashMap<>() {{
            put("code","ERR_INVALID_ENCRYPTION");
            put("error","");
            put("trace","Recipient Invalid Encryption");
        }});
        return obj;
    }
}
