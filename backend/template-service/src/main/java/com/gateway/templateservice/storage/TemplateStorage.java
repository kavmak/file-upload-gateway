package com.gateway.templateservice.storage;

import com.gateway.templateservice.model.TemplateEntity;
import com.gateway.templateservice.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
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
