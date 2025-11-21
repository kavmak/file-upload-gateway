package com.example.gateway.service;

import com.example.gateway.dto.TemplateDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.Arrays;

@Service
public class TemplateLookupService {

    @Value("${template.service.url}")
    private String templateServiceUrl;

    private final RestTemplate rest = new RestTemplate();

    /** --------------------------------------------------------------
     * 1. Fetch ALL template entities from Template-Service
     * -------------------------------------------------------------- */
    public Object fetchAllTemplatesRaw() {
        String url = templateServiceUrl + "/templates";
        return rest.getForObject(url, Object.class);
    }

    /** --------------------------------------------------------------
     * 2. Convert raw template JSON → List<String> categories
     * -------------------------------------------------------------- */
    public List<String> extractCategories(Object raw) {
        if (raw == null) return Collections.emptyList();

        List<String> categories = new ArrayList<>();

        if (raw instanceof List) {
            List<?> list = (List<?>) raw;

            for (Object o : list) {
                if (o instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) o;

                    Object category = m.get("category");
                    if (category != null) {
                        categories.add(String.valueOf(category));
                    }
                }
            }
        }
        return categories;
    }

    /** --------------------------------------------------------------
     * 3. Fetch metadata for app-specific template
     * -------------------------------------------------------------- */
    public TemplateDefinition fetchAppTemplate(String appNameHash, String category) {
        String url = templateServiceUrl + "/templates/app/" + appNameHash + "/" + category + "/metadata";
        
        System.out.println("➡ Fetching app template metadata from: " + url);
        
        try {
            return rest.getForObject(url, TemplateDefinition.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch app template metadata: " + e.getMessage());
        }
    }
    
    /** --------------------------------------------------------------
     * 4. Fetch categories for specific app
     * -------------------------------------------------------------- */
    public List<String> fetchAppCategories(String appNameHash) {
        String url = templateServiceUrl + "/templates/app/" + appNameHash + "/categories";
        
        System.out.println("➡ Fetching app categories from: " + url);
        
        try {
            String[] categories = rest.getForObject(url, String[].class);
            return categories != null ? Arrays.asList(categories) : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch app categories: " + e.getMessage());
        }
    }
    
    /** --------------------------------------------------------------
     * 5. Legacy method - Fetch metadata for a specific template category
     * -------------------------------------------------------------- */
    @Deprecated
    public TemplateDefinition fetchTemplate(String category) {
        String url = templateServiceUrl + "/templates/" + category + "/metadata";
        
        System.out.println("➡ Fetching template metadata from: " + url);
        
        try {
            return rest.getForObject(url, TemplateDefinition.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch template metadata: " + e.getMessage());
        }
    }
}
