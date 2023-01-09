package org.swasth.common.exception;

import org.junit.Test;

public class OTPVerificationExceptionTest {

    @Test(expected = OTPVerificationException.class)
    public void testOTPVerificationException() throws OTPVerificationException {
        throw new OTPVerificationException("Invalid otp");
    }

    @Test(expected = OTPVerificationException.class)
    public void testOTPVerificationExceptionWithErrorCode() throws OTPVerificationException {
        throw new OTPVerificationException(ErrorCodes.ERR_INVALID_OTP, "Invalid otp");
    }
}

