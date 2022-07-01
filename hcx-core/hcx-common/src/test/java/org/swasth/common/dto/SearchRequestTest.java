package org.swasth.common.dto;

import org.junit.Test;
import org.swasth.common.utils.Constants;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class SearchRequestTest {

    @Test
    public void testCreateSearchRequest() throws Exception {
        Request request = new SearchRequest(new HashMap<>(), Constants.COVERAGE_ELIGIBILITY_CHECK);
        assertEquals(Constants.COVERAGE_ELIGIBILITY_CHECK, request.getApiAction());
    }
}
