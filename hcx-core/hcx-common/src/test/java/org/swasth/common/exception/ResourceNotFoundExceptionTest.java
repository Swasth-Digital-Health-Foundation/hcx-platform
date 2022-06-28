package org.swasth.common.exception;

import org.junit.Test;

public class ResourceNotFoundExceptionTest {

    @Test(expected = ResourceNotFoundException.class)
    public void testResourceNotFoundException() throws ResourceNotFoundException {
        throw new ResourceNotFoundException("Resource is not found");
    }
}
