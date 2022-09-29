package io.hcxprotocol.model;

import io.hcxprotocol.dto.HCXIntegrator;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Map;

import static io.hcxprotocol.utils.Constants.*;

public class JSONRequest extends BaseRequest {

    public JSONRequest(Map<String, Object> payload) throws Exception {
        super(payload);
    }

    public boolean validateRedirect(Map<String, Object> error) {
        if (!validateCondition(StringUtils.isEmpty(getRedirectTo()), error, HCXIntegrator.ERROR_CODES.ERR_INVALID_REDIRECT_TO.toString(), MessageFormat.format(INVALID_REDIRECT_ERR_MSG, REDIRECT_TO)))
            return true;
        if (!validateCondition(getHcxSenderCode().equalsIgnoreCase(getRedirectTo()), error, HCXIntegrator.ERROR_CODES.ERR_INVALID_REDIRECT_TO.toString(), INVALID_REDIRECT_SELF_ERR_MSG))
            return true;
        return false;
    }

}
