package com.gateway.templateservice.model;

import javax.persistence.*;
import java.util.Map;

@Entity
@Table(name = "templates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"app_name_hash", "category"})
})
public class TemplateEntity {
    
    @Id
    private String id;
    
    @Column(name = "app_name", nullable = false)
    private String appName; // Original app name
    
    @Column(name = "app_name_hash", nullable = false)
    private String appNameHash; // Hashed app name for URL
    
    @Column(name = "endpoint", nullable = false)
    private String endpoint; // API endpoint to send JSON data
    
    @Column(nullable = false)
    private String category;
    
    @Column(name = "original_file_name")
    private String originalFileName;
    
    @Column(name = "file_type")
    private String fileType;
    
    @Lob
    @Column(name = "file_content")
    private byte[] fileContent;
    
    @Lob
    @Column(name = "extracted_json")
    private String extractedJson; // JSON string
    
    @Lob
    @Column(name = "metadata_json")
    private String metadataJson; // JSON string

    public TemplateEntity() {
    }

    // getters & setters (omitted for brevity - add all)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppNameHash() {
        return appNameHash;
    }

    public void setAppNameHash(String appNameHash) {
        this.appNameHash = appNameHash;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getExtractedJson() {
        return extractedJson;
    }

    public void setExtractedJson(String extractedJson) {
        this.extractedJson = extractedJson;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }
}
