package com.gateway.templateservice.service.impl;

import com.gateway.templateservice.dto.RegistrationRequest;
import com.gateway.templateservice.dto.RegistrationResponse;
import com.gateway.templateservice.dto.TemplateMetadataResponse;
import com.gateway.templateservice.dto.TemplateResponse;
import com.gateway.templateservice.util.HashUtil;
import com.gateway.templateservice.model.StructureRules;
import com.gateway.templateservice.model.TemplateEntity;
import com.gateway.templateservice.parser.CSVParser;
import com.gateway.templateservice.parser.PipeParser;
import com.gateway.templateservice.parser.TxtParser;
import com.gateway.templateservice.parser.ExcelParser;
import com.gateway.templateservice.service.TemplateService;
import com.gateway.templateservice.storage.TemplateStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateStorage storage;
    
    @Autowired
    private HashUtil hashUtil;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public RegistrationResponse registerApp(RegistrationRequest request, MultipartFile templateFile) {
        try {
            String appNameHash = hashUtil.hashAppName(request.getAppName());
            
            // Check if app-category combination already exists
            if (storage.existsByAppHashAndCategory(appNameHash, request.getCategory())) {
                // Update existing template
                TemplateEntity existingTemplate = storage.findByAppHashAndCategory(appNameHash, request.getCategory());
                updateTemplate(existingTemplate, templateFile, request);
                return new RegistrationResponse(true, "Template updated successfully", appNameHash);
            } else {
                // Create new template
                TemplateEntity newTemplate = createTemplate(templateFile, request, appNameHash);
                storage.save(newTemplate, templateFile.getBytes());
                return new RegistrationResponse(true, "App registered successfully", appNameHash);
            }
        } catch (Exception e) {
            return new RegistrationResponse(false, "Registration failed: " + e.getMessage(), null);
        }
    }
    
    @Override
    public List<String> getCategoriesByAppHash(String appNameHash) {
        List<TemplateEntity> templates = storage.findByAppHash(appNameHash);
        return templates.stream()
                .map(TemplateEntity::getCategory)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public TemplateMetadataResponse getMetadataByAppAndCategory(String appNameHash, String category) {
        TemplateEntity t = storage.findByAppHashAndCategory(appNameHash, category);
        if (t == null)
            throw new RuntimeException("Template not found for app and category");
        try {
            TemplateMetadataResponse resp = new TemplateMetadataResponse();
            resp.setTemplateName(t.getOriginalFileName());
            resp.setFileType(t.getFileType());
            resp.setEndpoint(t.getEndpoint());
            
            Map<String, Object> metaMap = mapper.readValue(t.getMetadataJson(), Map.class);
            List<String> headers = (List<String>) metaMap.get("headers");
            resp.setHeaders(headers);
            resp.setStructureRules((Map<String, Object>) metaMap.get("rules"));
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public Object getJsonByAppAndCategory(String appNameHash, String category) {
        TemplateEntity t = storage.findByAppHashAndCategory(appNameHash, category);
        if (t == null)
            throw new RuntimeException("Template not found for app and category");
        try {
            return mapper.readValue(t.getExtractedJson(), Object.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public byte[] downloadByAppAndCategory(String appNameHash, String category) {
        TemplateEntity t = storage.findByAppHashAndCategory(appNameHash, category);
        if (t == null)
            return null;
        return storage.getFileBytes(t);
    }
    
    @Override
    public TemplateEntity getByAppAndCategory(String appNameHash, String category) {
        return storage.findByAppHashAndCategory(appNameHash, category);
    }

    @Override
    @Deprecated
    public TemplateResponse uploadTemplate(MultipartFile file, String category, String format, String templateName) {
        try {
            String ext = format == null || format.trim().isEmpty() ? getExt(file.getOriginalFilename())
                    : format.toLowerCase();
            TemplateEntity entity = new TemplateEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setCategory(category);
            entity.setOriginalFileName(templateName == null ? file.getOriginalFilename() : templateName);
            entity.setFileType(ext);

            byte[] bytes = file.getBytes();
            Object parsed = parseBytes(bytes, ext);
            entity.setExtractedJson(mapper.writeValueAsString(parsed));

            Map<String, Object> meta = buildMetadata(parsed);
            entity.setMetadataJson(mapper.writeValueAsString(meta));

            storage.save(entity, bytes);

            TemplateResponse resp = new TemplateResponse();
            resp.setId(entity.getId());
            resp.setCategory(entity.getCategory());
            resp.setFileName(entity.getOriginalFileName());
            resp.setFileType(entity.getFileType());
            resp.setMessage("uploaded");
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<TemplateEntity> listAll() {
        return storage.findAll();
    }

    @Override
    public TemplateEntity getById(String id) {
        return storage.findById(id);
    }

    @Override
    public void delete(String id) {
        storage.delete(id);
    }

    @Override
    @Deprecated
    public TemplateMetadataResponse getMetadataByCategory(String category) {
        TemplateEntity t = storage.findByCategory(category);
        if (t == null)
            throw new RuntimeException("not found");
        try {
            TemplateMetadataResponse resp = new TemplateMetadataResponse();
            resp.setTemplateName(t.getOriginalFileName());
            resp.setFileType(t.getFileType());
            
            Map<String, Object> metaMap = mapper.readValue(t.getMetadataJson(), Map.class);
            List<String> headers = (List<String>) metaMap.get("headers");
            resp.setHeaders(headers);
            resp.setStructureRules((Map<String, Object>) metaMap.get("rules"));
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Deprecated
    public Object getJsonByCategory(String category) {
        TemplateEntity t = storage.findByCategory(category);
        if (t == null)
            throw new RuntimeException("not found");
        try {
            return mapper.readValue(t.getExtractedJson(), Object.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Deprecated
    public byte[] downloadByCategoryAndFormat(String category, String format) {
        TemplateEntity t = storage.findByCategory(category);
        if (t == null)
            return null;
        return storage.getFileBytes(t);
    }

    @Override
    public boolean pushMetadata(String id, String url) {
        TemplateEntity t = storage.findById(id);
        if (t == null)
            return false;
        try {
            RestTemplate rt = new RestTemplate();
            Object metaObj = mapper.readValue(t.getMetadataJson(), Object.class);
            rt.postForObject(url, metaObj, String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------- helpers ----------
    private Object parseBytes(byte[] bytes, String ext) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        System.out.println("🔍 Parsing file with extension: " + ext);
        
        switch (ext.toLowerCase()) {
            case "csv":
                System.out.println("📈 Using CSV parser");
                return CSVParser.parse(bis);
            case "txt":
                System.out.println("📄 Using TXT parser");
                return TxtParser.parse(bis);
            case "psv":
            case "pipe":
                System.out.println("📄 Using Pipe parser");
                return PipeParser.parse(bis);
            case "xls":
            case "xlsx":
                System.out.println("📊 Using Excel parser");
                return ExcelParser.parse(bis);
            default:
                System.err.println("❌ Unsupported file extension: " + ext);
                throw new RuntimeException("Unsupported file type: " + ext);
        }
    }

    private Map<String, Object> buildMetadata(Object parsed) {
        Map<String, Object> meta = new LinkedHashMap<>();
        List<Map<String, String>> rows = (List<Map<String, String>>) parsed;
        List<String> headers = new ArrayList<>();
        if (!rows.isEmpty())
            headers.addAll(rows.get(0).keySet());
        meta.put("headers", headers);

        Map<String, Object> rules = new LinkedHashMap<>();
        rules.put("minRows", 1);
        rules.put("maxRows", 10000);
        rules.put("strictColumnOrder", true);
        rules.put("allowExtraColumns", false);

        meta.put("rules", rules);
        return meta;
    }

    private TemplateEntity createTemplate(MultipartFile file, RegistrationRequest request, String appNameHash) throws Exception {
        String ext = getExt(file.getOriginalFilename());
        System.out.println("📁 Creating template - File: " + file.getOriginalFilename() + ", Extension: " + ext);
        
        TemplateEntity entity = new TemplateEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setAppName(request.getAppName());
        entity.setAppNameHash(appNameHash);
        entity.setEndpoint(request.getEndpoint());
        entity.setCategory(request.getCategory());
        entity.setOriginalFileName(file.getOriginalFilename());
        entity.setFileType(ext);

        byte[] bytes = file.getBytes();
        System.out.println("📁 File size: " + bytes.length + " bytes");
        
        try {
            Object parsed = parseBytes(bytes, ext);
            System.out.println("✅ Parsing successful");
            
            entity.setExtractedJson(mapper.writeValueAsString(parsed));
            Map<String, Object> meta = buildMetadata(parsed);
            entity.setMetadataJson(mapper.writeValueAsString(meta));
            
        } catch (Exception e) {
            System.err.println("❌ Parsing failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return entity;
    }
    
    private void updateTemplate(TemplateEntity existingTemplate, MultipartFile file, RegistrationRequest request) throws Exception {
        String ext = getExt(file.getOriginalFilename());
        existingTemplate.setEndpoint(request.getEndpoint());
        existingTemplate.setOriginalFileName(file.getOriginalFilename());
        existingTemplate.setFileType(ext);

        byte[] bytes = file.getBytes();
        Object parsed = parseBytes(bytes, ext);
        existingTemplate.setExtractedJson(mapper.writeValueAsString(parsed));

        Map<String, Object> meta = buildMetadata(parsed);
        existingTemplate.setMetadataJson(mapper.writeValueAsString(meta));
        
        storage.save(existingTemplate, bytes);
    }

    private String getExt(String filename) {
        if (filename == null || !filename.contains("."))
            return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
