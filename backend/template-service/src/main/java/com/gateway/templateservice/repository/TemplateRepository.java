package com.gateway.templateservice.repository;

import com.gateway.templateservice.model.TemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateEntity, String> {
    
    // Find by app hash and category (main query for app-specific templates)
    Optional<TemplateEntity> findByAppNameHashAndCategory(String appNameHash, String category);
    
    // Find all categories for a specific app
    List<TemplateEntity> findByAppNameHash(String appNameHash);
    
    // Find by original app name (for internal operations)
    List<TemplateEntity> findByAppName(String appName);
    
    // Check if app-category combination exists
    boolean existsByAppNameHashAndCategory(String appNameHash, String category);
    
    // Legacy methods (keep for backward compatibility during migration)
    @Deprecated
    Optional<TemplateEntity> findByCategory(String category);
    
    @Deprecated
    Optional<TemplateEntity> findByCategoryAndFileType(String category, String fileType);
}