package org.swasth.common.dto;

import java.util.List;

public class OnboardValidations {
    boolean emailEnabled;
    boolean phoneEnabled;
    List<String> onboardValidations ;
    public OnboardValidations(List<String> onboardValidations) {
        this.onboardValidations = onboardValidations;
        setEmailEnabled(checkEmailEnabled(onboardValidations, "email"));
        setPhoneEnabled(checkEmailEnabled(onboardValidations, "phone"));
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
    private static boolean checkEmailEnabled(List<String> validations, String key) {
        for (String validation : validations) {
            if (validation.equals(key)) {
                return true;
            }
        }
        return false;
    }

}
