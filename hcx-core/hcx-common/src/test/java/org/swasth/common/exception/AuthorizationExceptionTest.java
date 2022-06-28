package org.swasth.common.exception;

import org.junit.Test;

public class AuthorizationExceptionTest {

    @Test(expected = AuthorizationException.class)
    public void testAuthorizationException() throws AuthorizationException {
        throw new AuthorizationException("Invalid authorization");
    }
}
