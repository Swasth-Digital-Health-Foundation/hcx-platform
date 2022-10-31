package io.hcxprotocol.init;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
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
        validateConfig();
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

    private static void validateConfig() throws Exception {
        List<String> props = Arrays.asList("protocolBasePath", "participantCode", "authBasePath", "username", "password", "encryptionPrivateKey", "igUrl");
        for(String prop: props){
            if(!config.hasPathOrNull(prop) || StringUtils.isEmpty(config.getString(prop)))
                throw new Exception(prop + " is missing or has empty value, please add to the configuration.");
        }
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
