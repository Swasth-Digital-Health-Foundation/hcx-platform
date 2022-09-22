package io.hcxprotocol.dto;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class HCXIntegrator {

    private static HCXIntegrator hcxIntegrator = null;

    private HCXIntegrator(){}

    public static HCXIntegrator getInstance() {
        if (hcxIntegrator == null)
            hcxIntegrator = new HCXIntegrator();
        return hcxIntegrator;
    }

    Config config = ConfigFactory.load();

    public String getHCXBaseUrl(){
        return config.getString("hcx.base.url");
    }

}
