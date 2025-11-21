package com.gateway.templateservice.dto;

import java.util.List;
import java.util.Map;

public class TemplateMetadataResponse {
    private String templateName;
    private String fileType;
    private List<String> headers;
    private Map<String, Object> structureRules;
    private String endpoint;

    public TemplateMetadataResponse() {
    }

    // getters & setters
    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getStructureRules() {
        return structureRules;
    }

    public void setStructureRules(Map<String, Object> structureRules) {
        this.structureRules = structureRules;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
