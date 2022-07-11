package org.swasth.common.utils;

import org.junit.Test;
import org.swasth.common.exception.ErrorCodes;
import java.util.List;
import static org.junit.Assert.assertTrue;

public class ConstantsTest {

    @Test
    public void testDebugValues(){
        List<String> debugFlagValues = Constants.DEBUG_FLAG_VALUES;
        assertTrue(debugFlagValues.contains("Error"));
        assertTrue(debugFlagValues.contains("Info"));
        assertTrue(debugFlagValues.contains("Debug"));
    }

    @Test
    public void testRequestStatusValues(){
        List<String> debugFlagValues = Constants.REQUEST_STATUS_VALUES;
        assertTrue(debugFlagValues.contains("request.queued"));
        assertTrue(debugFlagValues.contains("request.dispatched"));
    }

    @Test
    public void testErrorDetailsValues(){
        List<String> debugFlagValues = Constants.ERROR_DETAILS_VALUES;
        assertTrue(debugFlagValues.contains("code"));
    }

    @Test
    public void testResponseStatusValues(){
        List<String> debugFlagValues = Constants.RESPONSE_STATUS_VALUES;
        assertTrue(debugFlagValues.contains("response.error"));
        assertTrue(debugFlagValues.contains("response.complete"));
    }

    @Test
    public void testOperationalEntities(){
        List<String> debugFlagValues = Constants.OPERATIONAL_ENTITIES;
        assertTrue(debugFlagValues.contains("notification"));
        assertTrue(debugFlagValues.contains("communication"));
    }

    @Test
    public void testExcludeEntities(){
        List<String> debugFlagValues = Constants.EXCLUDE_ENTITIES;
        assertTrue(debugFlagValues.contains("notification"));
        assertTrue(debugFlagValues.contains("paymentnotice"));
    }

    @Test
    public void testErrorValues(){
        List<String> debugFlagValues = Constants.RECIPIENT_ERROR_VALUES;
        assertTrue(debugFlagValues.contains(ErrorCodes.ERR_INVALID_WORKFLOW_ID.toString()));
        assertTrue(debugFlagValues.contains(ErrorCodes.ERR_INVALID_PAYLOAD.toString()));
        assertTrue(debugFlagValues.contains(ErrorCodes.ERR_INVALID_WORKFLOW_ID.toString()));
        assertTrue(debugFlagValues.contains(ErrorCodes.ERR_INVALID_CORRELATION_ID.toString()));
    }
}
