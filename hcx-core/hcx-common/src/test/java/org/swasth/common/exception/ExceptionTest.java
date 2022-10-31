package org.swasth.common.exception;

import org.junit.Test;
import org.swasth.common.exception.ErrorCodes;

import static org.junit.Assert.assertEquals;


public class ExceptionTest {

    @Test
    public void check_errorcodes() throws Exception {
        assertEquals("ERR_INVALID_PARTICIPANT_DETAILS", ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS.name());
        assertEquals("ERR_SERVICE_UNAVAILABLE", ErrorCodes.ERR_SERVICE_UNAVAILABLE.name());
        assertEquals("INTERNAL_SERVER_ERROR", ErrorCodes.INTERNAL_SERVER_ERROR.name());
        assertEquals("SERVER_ERR_GATEWAY_TIMEOUT", ErrorCodes.SERVER_ERR_GATEWAY_TIMEOUT.name());
        assertEquals("ERR_INVALID_SEARCH", ErrorCodes.ERR_INVALID_SEARCH.name());
        assertEquals("ERR_INVALID_CORRELATION_ID", ErrorCodes.ERR_INVALID_CORRELATION_ID.name());
        assertEquals("ERR_INVALID_WORKFLOW_ID", ErrorCodes.ERR_INVALID_WORKFLOW_ID.name());
        assertEquals("ERR_INVALID_PAYLOAD", ErrorCodes.ERR_INVALID_PAYLOAD.name());

    }
}
