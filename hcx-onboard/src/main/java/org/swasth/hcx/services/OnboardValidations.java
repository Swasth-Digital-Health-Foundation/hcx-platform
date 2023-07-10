package org.swasth.hcx.services;

import java.util.Map;

public class OnboardValidations {
    boolean emailEnabled;
    boolean phoneEnabled;
    Map<String,Object> onboardValidations ;
    public OnboardValidations(Map<String,Object> onboardValidations) {
        this.onboardValidations = onboardValidations;
        checkEmailAndPhoneEnabled(onboardValidations);
    }
    public boolean isEmailEnabled() {
        return emailEnabled;
    }
    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public boolean isPhoneEnabled() {
        return phoneEnabled;
    }
    public void setPhoneEnabled(boolean phoneEnabled) {
        this.phoneEnabled = phoneEnabled;
    }
    private void checkEmailAndPhoneEnabled(Map<String, Object> validations) {
        if (validations.get("email").equals("activation")) {
            setEmailEnabled(true);
        }
        if (validations.get("phone").equals("activation")) {
            setPhoneEnabled(true);
        }
    }

}
