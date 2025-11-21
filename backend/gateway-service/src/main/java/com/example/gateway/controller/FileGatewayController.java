package com.example.gateway.controller;

import com.example.gateway.dto.ExtractedFile;
import com.example.gateway.dto.TemplateDefinition;
import com.example.gateway.dto.StructureRules;
import com.example.gateway.dto.UploadResponse;

import com.example.gateway.service.EndpointNotificationService;
import com.example.gateway.service.ExtractionService;
import com.example.gateway.service.FileValidationService;
import com.example.gateway.service.StructureValidationService;
import com.example.gateway.service.TemplateLookupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
public class FileGatewayController {

    @Autowired
    private FileValidationService validationService;

    @Autowired
    private TemplateLookupService templateLookupService;

    @Autowired
    private ExtractionService extractionService;

    @Autowired
    private StructureValidationService structureValidationService;
    
    @Autowired
    private EndpointNotificationService endpointNotificationService;


    /** ---------------------------------------------------------
     *  GET APP-SPECIFIC TEMPLATE CATEGORIES
     *  --------------------------------------------------------- */
    @GetMapping("/templates/categories/{appNameHash}")
    public ResponseEntity<?> fetchAppCategories(@PathVariable String appNameHash) {
        try {
            List<String> categories = templateLookupService.fetchAppCategories(appNameHash);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Failed to fetch categories for app: " + e.getMessage());
        }
    }
    
    /** ---------------------------------------------------------
     *  GET ALL TEMPLATE CATEGORIES (Legacy - Deprecated)
     *  --------------------------------------------------------- */
    @GetMapping("/templates/categories")
    @Deprecated
    public ResponseEntity<?> fetchCategories() {
        try {
            Object raw = templateLookupService.fetchAllTemplatesRaw();
            List<String> categories = templateLookupService.extractCategories(raw);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Failed to fetch categories: " + e.getMessage());
        }
    }


    /** ---------------------------------------------------------
     *  FILE UPLOAD → Validate using Template-Service category
     *  --------------------------------------------------------- */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("application") String application,
            @RequestParam("category") String category,
            @RequestParam("appNameHash") String appNameHash,
            @RequestParam(value = "delimiter", required = false) String delimiter
    ) {

        try {
            // STEP 1: Validate file existence, size, allowed app, etc.
            validationService.validate(file, application, null);

            // STEP 2: Fetch template metadata from Template-Service using app-specific endpoint
            TemplateDefinition template = templateLookupService.fetchAppTemplate(appNameHash, category);

            if (template == null) {
                return ResponseEntity.status(500)
                        .body(new UploadResponse(false, "Template metadata is null!", null));
            }

            System.out.println("📌 TEMPLATE METADATA RECEIVED:");
            System.out.println("Template Name = " + template.getTemplateName());
            System.out.println("File Type = " + template.getFileType());
            System.out.println("Headers = " + template.getHeaders());
            System.out.println("Rules = " + template.getStructureRules());

            // STEP 2B: Convert map → StructureRules Java object
            StructureRules rules = convertRules(template.getStructureRules());


            // STEP 3: Extract file contents (headers + rows)
            Character delim = (delimiter != null && !delimiter.isEmpty()) 
                                ? delimiter.charAt(0) : null;

            ExtractedFile extracted = extractionService.extractWithHeaders(
                    file,
                    template.getFileType(),
                    delim
            );


            // STEP 4A: Validate headers
            structureValidationService.validateStructure(
                    template.getHeaders(),
                    extracted.getHeaders(),
                    rules
            );

            // STEP 4B: Validate row counts
            structureValidationService.validateRowCount(
                    extracted.getRows(),
                    rules
            );

            // STEP 5: Send data to registered app endpoint
            boolean notificationSent = endpointNotificationService.sendDataToAppEndpoint(
                    appNameHash, category, extracted.getRows()
            );
            
            String message = notificationSent ? 
                    "File validated and data sent to application successfully" : 
                    "File validated but failed to notify application";
            
            // STEP 6: Return extracted JSON
            return ResponseEntity.ok(
                    new UploadResponse(true, message, extracted.getRows())
            );

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(
                    new UploadResponse(false, ex.getMessage(), null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new UploadResponse(false, "Error: " + e.getMessage(), null)
            );
        }
    }


    /** ---------------------------------------------------------
     *  Convert Template-Service "structureRules" → StructureRules POJO
     *  --------------------------------------------------------- */
    private StructureRules convertRules(Map<String, Object> map) {
        if (map == null) return null;

        StructureRules r = new StructureRules();
        r.setMinRows((Integer) map.getOrDefault("minRows", 0));
        r.setMaxRows((Integer) map.getOrDefault("maxRows", Integer.MAX_VALUE));
        r.setStrictColumnOrder((Boolean) map.getOrDefault("strictColumnOrder", false));
        r.setAllowExtraColumns((Boolean) map.getOrDefault("allowExtraColumns", false));

        return r;
    }
}
