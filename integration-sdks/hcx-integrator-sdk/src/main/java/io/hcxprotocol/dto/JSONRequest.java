package io.hcxprotocol.dto;

import io.hcxprotocol.exception.ErrorCodes;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Map;

import static io.hcxprotocol.utils.Constants.REDIRECT_TO;
import static io.hcxprotocol.utils.ResponseMessage.INVALID_REDIRECT_ERR_MSG;
import static io.hcxprotocol.utils.ResponseMessage.INVALID_REDIRECT_SELF_ERR_MSG;

/**
 * This is to handle json payload specific validations for error and redirect scenarios.
 */
public class JSONRequest extends BaseRequest {

    public JSONRequest(Map<String, Object> payload) throws Exception {
        super(payload);
    }

    public boolean validateRedirect(Map<String, Object> error) {
        if (!validateCondition(StringUtils.isEmpty(getRedirectTo()), error, ErrorCodes.ERR_INVALID_REDIRECT_TO.toString(), MessageFormat.format(INVALID_REDIRECT_ERR_MSG, REDIRECT_TO)))
            return true;
        if (!validateCondition(getHcxSenderCode().equalsIgnoreCase(getRedirectTo()), error, ErrorCodes.ERR_INVALID_REDIRECT_TO.toString(), INVALID_REDIRECT_SELF_ERR_MSG))
            return true;
        return false;
    }

}
