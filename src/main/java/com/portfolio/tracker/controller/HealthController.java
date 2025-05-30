package com.portfolio.tracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @GetMapping({"/health", "/health/"})
    public ResponseEntity<Map<String, Object>> health() {
        logger.debug("Health check endpoint called");
        
        Map<String, Object> response = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now().toString(),
            "service", "portfolio-tracker-backend"
        );
        
        logger.debug("Health check returning: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/ping", "/ping/"})
    public ResponseEntity<String> ping() {
        logger.debug("Ping endpoint called");
        return ResponseEntity.ok("pong");
    }
}
