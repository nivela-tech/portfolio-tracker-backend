package com.portfolio.tracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {    // Add additional configurations for URL path handling
    
    @Value("${spring.web.cors.allowed-origins}")
    private String allowedOrigins;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins) // Use value from properties file
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Added OPTIONS for CORS preflight
                .allowedHeaders("*") // All headers allowed
                .exposedHeaders("Set-Cookie", "Authorization", "X-XSRF-TOKEN") // Expose cookie headers to frontend
                .allowCredentials(true) // Allow credentials (cookies, authorization headers)
                .maxAge(3600); // Cache preflight requests for 1 hour (3600 seconds)
    }
}
