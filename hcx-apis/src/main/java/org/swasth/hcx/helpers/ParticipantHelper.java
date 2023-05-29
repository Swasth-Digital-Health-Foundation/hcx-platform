package org.swasth.hcx.helpers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.Token;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;

import java.util.List;

@Service
public class ParticipantHelper {

    public void authorizeEntity(String authToken, String participantCode, String email) throws Exception {
        Token token = new Token(authToken);
        if(validateRoles(token.getRoles())){
            if(!StringUtils.equalsIgnoreCase(token.getUsername(), email))
                throw new ClientException(ErrorCodes.ERR_ACCESS_DENIED, "Invalid authorization token");
        } else if (token.getRoles().contains(Constants.CONFIG_MANAGER)) {
            // TODO: fetch user details and validate
        }
    }

    private boolean validateRoles(List<String> tokenRoles) {
        for(String role: tokenRoles) {
            if(Constants.PARTICIPANT_ROLES.contains(role))
                return true;
        }
        return false;
    }

}
