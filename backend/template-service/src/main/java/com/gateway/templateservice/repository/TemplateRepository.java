package com.gateway.templateservice.repository;

import com.gateway.templateservice.model.TemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateEntity, String> {
    
    Optional<TemplateEntity> findByCategory(String category);
    
    Optional<TemplateEntity> findByCategoryAndFileType(String category, String fileType);
}