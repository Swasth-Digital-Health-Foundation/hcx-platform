package org.swasth.hcx.pojo;

import org.swasth.common.exception.ClientException;

import java.util.List;
import java.util.Map;

public class SearchRequestBody extends RequestBody {

    public SearchRequestBody(Map<String, Object> body) throws Exception {
        super(body);
    }

    @Override
    public void validate(List<String> mandatoryHeaders) throws ClientException {
        super.validate(mandatoryHeaders);
        // TODO search filter specific validation
    }
}
