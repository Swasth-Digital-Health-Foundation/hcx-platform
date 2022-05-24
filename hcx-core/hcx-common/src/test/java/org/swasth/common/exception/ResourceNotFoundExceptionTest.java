package org.swasth.common.exception;

import org.junit.Test;

public class ResourceNotFoundExceptionTest {

    @Test(expected = ResourceNotFoundException.class)
    void testResourceNotFoundException() throws ResourceNotFoundException {
        throw new ResourceNotFoundException("Resource is not found");
    }
}
