package com.example.ecommerce.controller;

import com.example.ecommerce.service.DataGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class DemoController {
    
    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);
    private static final Logger accessLogger = LoggerFactory.getLogger("ACCESS_LOGGER");
    
    @Autowired
    private DataGeneratorService dataGeneratorService;
    
    @PostMapping("/init-data")
    public ResponseEntity<Map<String, String>> initializeData() {
        long startTime = System.currentTimeMillis();
        
        try {
            dataGeneratorService.initializeProducts();
            long duration = System.currentTimeMillis() - startTime;
            
            accessLogger.info("POST /api/demo/init-data - 200 - {}ms - Sample data initialized", duration);
            return ResponseEntity.ok(Map.of("message", "Sample data initialized successfully"));
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error initializing sample data: {}", e.getMessage());
            accessLogger.error("POST /api/demo/init-data - 500 - {}ms - Error: {}", duration, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/simulate-traffic")
    public ResponseEntity<Map<String, String>> simulateTraffic(@RequestParam(defaultValue = "10") int requests) {
        long startTime = System.currentTimeMillis();
        
        try {
            dataGeneratorService.simulateUserTraffic(requests);
            long duration = System.currentTimeMillis() - startTime;
            
            accessLogger.info("POST /api/demo/simulate-traffic - 200 - {}ms - {} requests simulated", 
                            duration, requests);
            return ResponseEntity.ok(Map.of("message", requests + " requests simulated successfully"));
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error simulating traffic: {}", e.getMessage());
            accessLogger.error("POST /api/demo/simulate-traffic - 500 - {}ms - Error: {}", 
                             duration, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/simulate-errors")
    public ResponseEntity<Map<String, String>> simulateErrors(@RequestParam(defaultValue = "5") int errorCount) {
        long startTime = System.currentTimeMillis();
        
        try {
            dataGeneratorService.simulateErrors(errorCount);
            long duration = System.currentTimeMillis() - startTime;
            
            accessLogger.info("POST /api/demo/simulate-errors - 200 - {}ms - {} errors simulated", 
                            duration, errorCount);
            return ResponseEntity.ok(Map.of("message", errorCount + " errors simulated successfully"));
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error simulating errors: {}", e.getMessage());
            accessLogger.error("POST /api/demo/simulate-errors - 500 - {}ms - Error: {}", 
                             duration, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        long startTime = System.currentTimeMillis();
        long duration = System.currentTimeMillis() - startTime;
        
        accessLogger.info("GET /api/demo/health - 200 - {}ms - Health check OK", duration);
        return ResponseEntity.ok(Map.of("status", "healthy", "timestamp", java.time.LocalDateTime.now().toString()));
    }
}
