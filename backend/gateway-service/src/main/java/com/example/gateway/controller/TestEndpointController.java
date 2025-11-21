package com.example.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestEndpointController {

    /**
     * Test endpoint to simulate receiving data from the file upload utility
     * This simulates what external apps would implement to receive validated data
     */
    @PostMapping("/receive-data")
    public ResponseEntity<String> receiveData(@RequestBody List<Map<String, Object>> data) {
        System.out.println("🎯 TEST ENDPOINT: Received data from file upload utility");
        System.out.println("📊 Number of records: " + data.size());
        
        // Log first few records for demonstration
        for (int i = 0; i < Math.min(3, data.size()); i++) {
            System.out.println("Record " + (i + 1) + ": " + data.get(i));
        }
        
        if (data.size() > 3) {
            System.out.println("... and " + (data.size() - 3) + " more records");
        }
        
        return ResponseEntity.ok("Data received successfully by test application");
    }
    
    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Test endpoint is running and ready to receive data");
    }
}