package com.portfolio.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class PortfolioTrackerApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(PortfolioTrackerApplication.class);
    
    public static void main(String[] args) {
        logger.info("=== Starting Portfolio Tracker Application ===");
        logger.info("Java version: {}", System.getProperty("java.version"));
        logger.info("Active profile: {}", System.getProperty("spring.profiles.active", "default"));
        logger.info("DATABASE_URL present: {}", System.getenv("DATABASE_URL") != null);
        logger.info("PORT: {}", System.getenv("PORT"));
        
        try {
            SpringApplication.run(PortfolioTrackerApplication.class, args);
            logger.info("=== Application started successfully ===");
        } catch (Exception e) {
            logger.error("=== Application failed to start ===", e);
            throw e;
        }
    }
}
