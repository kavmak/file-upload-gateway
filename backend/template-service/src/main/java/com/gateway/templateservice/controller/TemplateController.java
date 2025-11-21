package com.gateway.templateservice.controller;

import com.gateway.templateservice.dto.TemplateMetadataResponse;
import com.gateway.templateservice.dto.TemplateResponse;
import com.gateway.templateservice.model.TemplateEntity;
import com.gateway.templateservice.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

@RestController
@RequestMapping("/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @PostMapping("/upload")
    public ResponseEntity<TemplateResponse> uploadTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam("format") String format,
            @RequestParam(value = "templateName", required = false) String templateName) {
        TemplateResponse resp = templateService.uploadTemplate(file, category, format, templateName);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<Collection<TemplateEntity>> listTemplates() {
        return ResponseEntity.ok(templateService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateEntity> getById(@PathVariable("id") String id) {
        TemplateEntity t = templateService.getById(id);
        if (t == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(t);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") String id) {
        templateService.delete(id);
        return ResponseEntity.ok("deleted");
    }

    // New app-specific endpoints
    @GetMapping("/app/{appNameHash}/categories")
    public ResponseEntity<java.util.List<String>> getAppCategories(@PathVariable String appNameHash) {
        java.util.List<String> categories = templateService.getCategoriesByAppHash(appNameHash);
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/app/{appNameHash}/{category}/metadata")
    public ResponseEntity<TemplateMetadataResponse> getAppMetadata(
            @PathVariable String appNameHash, 
            @PathVariable String category) {
        TemplateMetadataResponse meta = templateService.getMetadataByAppAndCategory(appNameHash, category);
        return ResponseEntity.ok(meta);
    }
    
    @GetMapping("/app/{appNameHash}/{category}/json")
    public ResponseEntity<Object> getAppJsonPreview(
            @PathVariable String appNameHash, 
            @PathVariable String category) {
        Object json = templateService.getJsonByAppAndCategory(appNameHash, category);
        return ResponseEntity.ok(json);
    }
    
    @GetMapping("/app/{appNameHash}/{category}/download")
    public ResponseEntity<InputStreamResource> downloadAppTemplate(
            @PathVariable String appNameHash, 
            @PathVariable String category) {
        
        byte[] data = templateService.downloadByAppAndCategory(appNameHash, category);
        if (data == null)
            return ResponseEntity.notFound().build();

        // Get template entity to extract original filename
        TemplateEntity template = templateService.getByAppAndCategory(appNameHash, category);
        String originalFileName = template.getOriginalFileName();
        String fileExtension = "csv"; // default
        
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.') + 1);
        }
        
        String fileName = category + "-template." + fileExtension;
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(data.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(new java.io.ByteArrayInputStream(data)));
    }

    // Legacy endpoints (deprecated)
    @GetMapping("/{category}/metadata")
    @Deprecated
    public ResponseEntity<TemplateMetadataResponse> metadata(@PathVariable("category") String category) {
        TemplateMetadataResponse meta = templateService.getMetadataByCategory(category);
        return ResponseEntity.ok(meta);
    }

    @GetMapping("/{category}/json")
    @Deprecated
    public ResponseEntity<Object> jsonPreview(@PathVariable("category") String category) {
        Object json = templateService.getJsonByCategory(category);
        return ResponseEntity.ok(json);
    }

    @GetMapping("/{category}/download")
    @Deprecated
    public ResponseEntity<InputStreamResource> download(
            @PathVariable("category") String category,
            @RequestParam("format") String format) {

        byte[] data = templateService.downloadByCategoryAndFormat(category, format);
        if (data == null)
            return ResponseEntity.notFound().build();

        String fileName = category + "-template." + format;
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(data.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(new java.io.ByteArrayInputStream(data)));
    }

    @PostMapping("/{id}/push")
    public ResponseEntity<String> push(@PathVariable("id") String id, @RequestParam("url") String url) {
        boolean ok = templateService.pushMetadata(id, url);
        if (ok)
            return ResponseEntity.ok("pushed");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed");
    }
}
