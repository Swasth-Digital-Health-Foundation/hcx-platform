package org.swasth.common.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VerificationExceptionTest {

    @Test(expected = VerificationException.class)
    public void testVerificationException() throws VerificationException {
        throw new VerificationException("Invalid otp");
    }

    @Test(expected = VerificationException.class)
    public void testVerificationExceptionWithErrorCode() throws VerificationException {
        throw new VerificationException(ErrorCodes.ERR_INVALID_LINK, "Invalid otp");
    }

    @Test
    public void getErrorcodeTest(){
        VerificationException verificationException = new VerificationException(ErrorCodes.ERR_INVALID_LINK,"Invalid otp");
        assertEquals(ErrorCodes.ERR_INVALID_LINK,verificationException.getErrCode());
    }
}

