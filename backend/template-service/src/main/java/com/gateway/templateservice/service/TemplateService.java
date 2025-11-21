package com.gateway.templateservice.service;

import com.gateway.templateservice.dto.RegistrationRequest;
import com.gateway.templateservice.dto.RegistrationResponse;
import com.gateway.templateservice.dto.TemplateMetadataResponse;
import com.gateway.templateservice.dto.TemplateResponse;
import com.gateway.templateservice.model.TemplateEntity;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

public interface TemplateService {
    // New registration methods
    RegistrationResponse registerApp(RegistrationRequest request, MultipartFile templateFile);
    
    // App-specific template methods
    List<String> getCategoriesByAppHash(String appNameHash);
    TemplateMetadataResponse getMetadataByAppAndCategory(String appNameHash, String category);
    Object getJsonByAppAndCategory(String appNameHash, String category);
    byte[] downloadByAppAndCategory(String appNameHash, String category);
    
    // Legacy methods (keep for backward compatibility)
    @Deprecated
    TemplateResponse uploadTemplate(MultipartFile file, String category, String format, String templateName);
    @Deprecated
    TemplateMetadataResponse getMetadataByCategory(String category);
    @Deprecated
    Object getJsonByCategory(String category);
    @Deprecated
    byte[] downloadByCategoryAndFormat(String category, String format);

    Collection<TemplateEntity> listAll();
    TemplateEntity getById(String id);
    TemplateEntity getByAppAndCategory(String appNameHash, String category);
    void delete(String id);
    boolean pushMetadata(String id, String url);
}
