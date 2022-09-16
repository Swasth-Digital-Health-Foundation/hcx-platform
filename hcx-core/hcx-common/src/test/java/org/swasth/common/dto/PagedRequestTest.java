package org.swasth.common.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PagedRequestTest {

    @Test
    public void testCreatePagedRequest(){
        PagedRequest request = new PagedRequest();
        request.setLimit(10);
        request.setOffset(5);
        assertEquals(10, request.getLimit());
        assertEquals(5, request.getOffset());
    }

}
