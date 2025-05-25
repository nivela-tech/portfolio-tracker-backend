package com.portfolio.tracker.controller;

import com.portfolio.tracker.dto.UserDto;
import com.portfolio.tracker.model.User;
import com.portfolio.tracker.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserDetails(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            logger.info("No authenticated user found in session.");
            // Not an error, just means user is not logged in.
            // Frontend will interpret a 401 or lack of user data as "not authenticated".
            // Spring Security by default might redirect to login page if this endpoint is secured
            // and no user is found, but since we want to return a specific status for XHR,
            // this check is useful. If SecurityConfig allows unauthenticated access to /api/user/me
            // then this check is critical. Otherwise, Spring Security handles unauthorized access.
            // For now, let's assume this endpoint is protected and oAuth2User will be populated if logged in.
            // If it's null despite protection, it's an issue, but typically Spring handles this.
            // A more explicit way for XHR is to return a specific DTO or an empty body with 200 OK
            // and let the frontend decide, or a 401 if preferred.
            // Let's return an empty UserDto or null, and let the frontend handle it.
            return ResponseEntity.ok(null); 
        }

        try {
            User user = userService.getUserByEmail(oAuth2User.getAttribute("email"));
            if (user == null) {
                // This should not happen if CustomOAuth2UserService is working correctly
                logger.error("Authenticated user {} not found in the database.", oAuth2User.getAttribute("email"));
                return ResponseEntity.internalServerError().body("User details not found in database.");
            }
            // Create a DTO to send to the frontend, excluding sensitive info if any
            UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getProvider());
            logger.info("Returning details for user: {}", userDto.getEmail());
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            logger.error("Error retrieving user details for {}: {}", oAuth2User.getAttribute("email"), e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error retrieving user details.");
        }
    }
}
