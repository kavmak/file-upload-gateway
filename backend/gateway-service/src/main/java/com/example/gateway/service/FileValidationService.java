package com.example.gateway.service;

import com.example.gateway.util.FileTypeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class FileValidationService {

    private final Set<String> allowedApps;

    // Size in MB (configured in application.properties)
    @Value("${gateway.max-file-mb}")
    private int maxFileMb;

    public FileValidationService(@Value("${gateway.allowed.applications}") String allowedAppsCsv) {
        this.allowedApps = new HashSet<>(Arrays.asList(allowedAppsCsv.split(",")));
    }

    public void validate(MultipartFile file, String application, String declaredFormat) {

        // 1. File must exist
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        // 2. Valid application (skip validation if wildcard is set)
        if (application == null) {
            throw new IllegalArgumentException("Application cannot be null.");
        }
        
        if (!allowedApps.contains("*") && !allowedApps.contains(application)) {
            throw new IllegalArgumentException("Application '" + application + "' is not allowed.");
        }

        // 3. File size in MB
        long fileSizeBytes = file.getSize();
        long maxBytes = maxFileMb * 1024L * 1024L;

        if (fileSizeBytes > maxBytes) {
            throw new IllegalArgumentException(
                    "File size exceeds the allowed limit of " + maxFileMb + " MB. " +
                    "Uploaded size = " + (fileSizeBytes / (1024 * 1024)) + " MB."
            );
        }

        // 4. Format validation (if declared)
        String detected = FileTypeUtil.detectTypeByName(file);

        if (declaredFormat != null && !declaredFormat.trim().isEmpty()) {
            if (!declaredFormat.equalsIgnoreCase(detected)) {
                throw new IllegalArgumentException(
                        "File format mismatch. Expected '" + declaredFormat +
                                "', detected '" + detected + "'."
                );
            }
        }
    }
}
