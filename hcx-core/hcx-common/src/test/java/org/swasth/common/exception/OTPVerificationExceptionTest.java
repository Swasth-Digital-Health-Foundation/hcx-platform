package org.swasth.common.exception;

import org.junit.Test;

public class OTPVerificationExceptionTest {

    @Test(expected = VerificationException.class)
    public void testOTPVerificationException() throws VerificationException {
        throw new VerificationException("Invalid otp");
    }

    @Test(expected = VerificationException.class)
    public void testOTPVerificationExceptionWithErrorCode() throws VerificationException {
        throw new VerificationException(ErrorCodes.ERR_INVALID_LINK, "Invalid otp");
    }
}

