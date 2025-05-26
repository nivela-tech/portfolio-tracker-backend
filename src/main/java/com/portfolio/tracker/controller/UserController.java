package com.portfolio.tracker.controller;

import com.portfolio.tracker.dto.UserDto;
import com.portfolio.tracker.model.User;
import com.portfolio.tracker.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;

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
            return ResponseEntity.ok(null); 
        }

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            logger.warn("Email attribute not found in OAuth2User principal for user: {}", oAuth2User.getName());
            return ResponseEntity.badRequest().body("User email not found in authentication token");
        }

        try {
            User user = userService.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Authenticated user with email {} not found in the database.", email);
                    return new EntityNotFoundException("User details not found in database for email: " + email);
                });

            UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getProviderId()); 
            logger.info("Returning details for user: {}", userDto.getEmail());
            return ResponseEntity.ok(userDto);
        } catch (EntityNotFoundException e) {
            logger.warn("EntityNotFoundException in getCurrentUserDetails: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } 
        catch (Exception e) {
            logger.error("Error retrieving user details for {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving user details.");
        }
    }
}
