package io.hcxprotocol.dto;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * The methods and variables to access the configuration. Enumeration of error codes and operations.
 */
public class HCXIntegrator {

    private static HCXIntegrator hcxIntegrator = null;

    private HCXIntegrator() {
    }

    public static HCXIntegrator getInstance() {
        if (hcxIntegrator == null)
            hcxIntegrator = new HCXIntegrator();
        return hcxIntegrator;
    }

    Config config = ConfigFactory.load();

    public String getHCXProtocolBasePath() {
        return config.getString("hcx.protocolBasePath");
    }

    public String getParticipantCode() {
        return config.getString("hcx.participantCode");
    }

    public String getAuthBasePath() {
        return config.getString("hcx.authBasePath");
    }

    public String getUsername() {
        return config.getString("hcx.username");
    }

    public String getPassword() {
        return config.getString("hcx.password");
    }

    public String getPrivateKey() {
        return config.getString("hcx.encryptionPrivateKey");
    }

    public String getIGUrl() {
        return config.getString("hcx.igUrl");
    }

}
