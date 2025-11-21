package com.gateway.templateservice.storage;

import com.gateway.templateservice.model.TemplateEntity;
import com.gateway.templateservice.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class TemplateStorage {

    @Autowired
    private TemplateRepository templateRepository;

    public TemplateEntity save(TemplateEntity t, byte[] content) {
        t.setFileContent(content);
        return templateRepository.save(t);
    }

    public TemplateEntity findById(String id) {
        Optional<TemplateEntity> template = templateRepository.findById(id);
        return template.orElse(null);
    }

    public TemplateEntity findByAppHashAndCategory(String appNameHash, String category) {
        Optional<TemplateEntity> template = templateRepository.findByAppNameHashAndCategory(appNameHash, category);
        return template.orElse(null);
    }
    
    public List<TemplateEntity> findByAppHash(String appNameHash) {
        return templateRepository.findByAppNameHash(appNameHash);
    }
    
    public boolean existsByAppHashAndCategory(String appNameHash, String category) {
        return templateRepository.existsByAppNameHashAndCategory(appNameHash, category);
    }
    
    // Legacy method (keep for backward compatibility)
    @Deprecated
    public TemplateEntity findByCategory(String category) {
        Optional<TemplateEntity> template = templateRepository.findByCategory(category);
        return template.orElse(null);
    }

    public Collection<TemplateEntity> findAll() {
        return templateRepository.findAll();
    }

    public void delete(String id) {
        templateRepository.deleteById(id);
    }

    public byte[] getFileBytes(TemplateEntity template) {
        return template.getFileContent();
    }
}
