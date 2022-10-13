package io.hcxprotocol.dto;

import java.util.Map;

/**
 * This is to handle jwe payload specific validations.
 */
public class JWERequest extends BaseRequest {

    public JWERequest(Map<String, Object> payload) throws Exception {
        super(payload);
    }
}
