package com.gateway.templateservice.dto;

public class RegistrationResponse {
    private boolean success;
    private String message;
    private String appNameHash;
    
    public RegistrationResponse() {}
    
    public RegistrationResponse(boolean success, String message, String appNameHash) {
        this.success = success;
        this.message = message;
        this.appNameHash = appNameHash;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getAppNameHash() {
        return appNameHash;
    }
    
    public void setAppNameHash(String appNameHash) {
        this.appNameHash = appNameHash;
    }
}