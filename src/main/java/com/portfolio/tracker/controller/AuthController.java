package com.portfolio.tracker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${spring.web.cors.allowed-origins}", allowedHeaders = "*", allowCredentials = "true", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS
})
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Value("${spring.web.cors.allowed-origins}")
    private String frontendUrl;

    /**
     * Comprehensive logout endpoint that ensures complete session invalidation
     * and proper cleanup of all authentication artifacts.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            HttpServletRequest request, 
            HttpServletResponse response,
            Authentication authentication) {
        
        logger.info("Processing logout request from user: {}", 
            authentication != null ? authentication.getName() : "anonymous");

        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. Get current session before invalidation for logging
            HttpSession session = request.getSession(false);
            String sessionId = session != null ? session.getId() : "none";
            
            // 2. Perform Spring Security logout which handles:
            // - Authentication clearing
            // - Session invalidation
            // - Security context clearing
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.setInvalidateHttpSession(true);
            logoutHandler.setClearAuthentication(true);
            logoutHandler.logout(request, response, authentication);
            
            // 3. Explicitly clear all authentication-related cookies
            clearAuthenticationCookies(response);
            
            // 4. Clear security context (additional safety)
            SecurityContextHolder.clearContext();
            
            // 5. Add security headers to prevent caching
            addSecurityHeaders(response);
            
            logger.info("Successfully logged out user. Session {} invalidated.", sessionId);
            
            result.put("success", true);
            result.put("message", "Logout successful");
            result.put("redirectUrl", frontendUrl + "/?logout=true");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error during logout process", e);
            result.put("success", false);
            result.put("message", "Logout completed with errors");
            result.put("redirectUrl", frontendUrl + "/?logout=true");
            
            // Even if there are errors, we should still redirect to login
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * Check if user is authenticated - useful for session validation
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(Authentication authentication) {
        Map<String, Object> status = new HashMap<>();
        
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        status.put("authenticated", isAuthenticated);
        
        if (isAuthenticated) {
            status.put("user", authentication.getName());
        }
        
        return ResponseEntity.ok(status);
    }

    /**
     * Session validation endpoint - helps detect stale sessions
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateSession(
            HttpServletRequest request,
            Authentication authentication) {
        
        Map<String, Object> validation = new HashMap<>();
        
        // Check if session exists and is valid
        HttpSession session = request.getSession(false);
        boolean hasValidSession = session != null && !session.isNew();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        
        validation.put("sessionValid", hasValidSession);
        validation.put("authenticated", isAuthenticated);
        validation.put("sessionId", hasValidSession ? session.getId() : null);
        validation.put("maxInactiveInterval", hasValidSession ? session.getMaxInactiveInterval() : 0);
        
        if (!hasValidSession || !isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }
        
        return ResponseEntity.ok(validation);
    }

    /**
     * Clear all authentication-related cookies with secure settings
     */
    private void clearAuthenticationCookies(HttpServletResponse response) {
        String[] cookiesToClear = {
            "JSESSIONID", 
            "XSRF-TOKEN", 
            "remember-me",
            "SESSION" // For Spring Session if used
        };
        
        for (String cookieName : cookiesToClear) {
            Cookie cookie = new Cookie(cookieName, null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            cookie.setHttpOnly(true);
            cookie.setSecure(true); // Enable in production with HTTPS
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);
            
            logger.debug("Cleared cookie: {}", cookieName);
        }
    }

    /**
     * Add security headers to prevent caching and enhance security
     */
    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
    }
}
