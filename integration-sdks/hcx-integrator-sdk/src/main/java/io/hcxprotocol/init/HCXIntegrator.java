package io.hcxprotocol.init;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Map;

/**
 * The methods and variables to access the configuration. Enumeration of error codes and operations.
 */
public class HCXIntegrator {

    private static HCXIntegrator hcxIntegrator = null;

    private static Config config = null;

    private HCXIntegrator() {
    }

    public static HCXIntegrator getInstance() throws Exception {
        if(config == null)
            throw new Exception("Please initialize the configuration variables, in order to initialize the SDK");
        if (hcxIntegrator == null)
            hcxIntegrator = new HCXIntegrator();
        return hcxIntegrator;
    }


    /**
     * This method is to initialize config factory by passing the configuration as Map.
     *
     * @param configMap A Map that contains configuration variables and its values.
     */
    public static void init(Map<String,Object> configMap) {
        config = ConfigFactory.parseMap(configMap);
    }


    /**
     * This method is to initialize config factory by passing the configuration as JSON String.
     *
     * @param configStr A String that contains configuration variables and its values in a JSON format.
     */
    public static void init(String configStr) {
        config = ConfigFactory.parseString(configStr);
    }

    public String getHCXProtocolBasePath() {
        return config.getString("protocolBasePath");
    }

    public String getParticipantCode() {
        return config.getString("participantCode");
    }

    public String getAuthBasePath() {
        return config.getString("authBasePath");
    }

    public String getUsername() {
        return config.getString("username");
    }

    public String getPassword() {
        return config.getString("password");
    }

    public String getPrivateKey() {
        return config.getString("encryptionPrivateKey");
    }

    public String getIGUrl() {
        return config.getString("igUrl");
    }

}
