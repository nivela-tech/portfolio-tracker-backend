package com.portfolio.tracker.controller;

import com.portfolio.tracker.model.PortfolioAccount;
import com.portfolio.tracker.model.User;
import com.portfolio.tracker.service.PortfolioAccountService;
import com.portfolio.tracker.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/accounts", "/api/accounts/"})  // Handle both with and without trailing slash
public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private PortfolioAccountService accountService;

    @Autowired
    private UserService userService;

    private User getAuthenticatedUser(OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            logger.warn("Attempt to access account resource without authentication.");
            throw new SecurityException("User not authenticated");
        }
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            logger.error("Email attribute not found in OAuth2User principal.");
            throw new SecurityException("User email not found in authentication token");
        }
        return userService.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Authenticated user with email {} not found in database.", email);
                    return new EntityNotFoundException("Authenticated user not found");
                });
    }    @PostMapping({"", "/"})  // Handle both with and without trailing slash
    public ResponseEntity<?> createAccount(@RequestBody PortfolioAccount account, @AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            User user = getAuthenticatedUser(oAuth2User);
            logger.info("Received request to create account: {} for user {}", account.getName(), user.getEmail());
            PortfolioAccount createdAccount = accountService.createAccount(account, user);
            logger.info("Successfully created account with ID: {} for user {}", createdAccount.getId(), user.getEmail());
            return ResponseEntity.ok(createdAccount);
        } catch (SecurityException e) {
            logger.warn("Security exception in createAccount: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.warn("EntityNotFoundException in createAccount: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid account data received for user {}: {}", (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating account for user {}: {}", (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllAccounts(@AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            User user = getAuthenticatedUser(oAuth2User);
            logger.info("Received request to get all accounts for user {}", user.getEmail());
            List<PortfolioAccount> accounts = accountService.getAllAccountsByUser(user);
            if (accounts.isEmpty()) {
                logger.info("No accounts found for user {}", user.getEmail());
                return ResponseEntity.ok(accounts); // Return empty list instead of error
            }
            logger.info("Retrieved {} accounts for user {}", accounts.size(), user.getEmail());
            return ResponseEntity.ok(accounts);
        } catch (SecurityException e) {
            logger.warn("Security exception in getAllAccounts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving all accounts for user {}: {}", (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable Long id, @AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            User user = getAuthenticatedUser(oAuth2User);
            logger.info("Received request to get account with ID: {} for user {}", id, user.getEmail());
            PortfolioAccount account = accountService.getAccountByIdAndUser(id, user);
            logger.info("Successfully retrieved account: {} for user {}", account.getName(), user.getEmail());
            return ResponseEntity.ok(account);
        } catch (SecurityException e) {
            logger.warn("Security exception in getAccountById (ID: {}): {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.warn("Account not found with ID: {} for user {}: {}", id, (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid account ID: {} for user {}: {}", id, (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving account with ID {} for user {}: {}", id, (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Long id, @RequestBody PortfolioAccount accountDetails, @AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            User user = getAuthenticatedUser(oAuth2User);
            logger.info("Received request to update account ID {} for user {}", id, user.getEmail());
            PortfolioAccount updatedAccount = accountService.updateAccount(id, accountDetails, user);
            logger.info("Successfully updated account ID {} for user {}", id, user.getEmail());
            return ResponseEntity.ok(updatedAccount);
        } catch (SecurityException e) {
            logger.warn("Security exception in updateAccount (ID: {}): {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.warn("EntityNotFoundException in updateAccount (ID: {}): {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid data for updating account ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating account ID {} for user {}: {}", id, (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }    @DeleteMapping({"/{id}", "/{id}/"}) // Handle both with and without trailing slash
    public ResponseEntity<?> deleteAccount(@PathVariable Long id, @AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            User user = getAuthenticatedUser(oAuth2User);
            logger.info("Received request to delete account ID {} by user {}", id, user.getEmail());
            accountService.deleteAccount(id, user);
            logger.info("Successfully deleted account ID {} for user {}", id, user.getEmail());
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            logger.warn("Security exception in deleteAccount (ID: {}): {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.warn("EntityNotFoundException in deleteAccount (ID: {}): {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("IllegalStateException in deleteAccount (ID: {}): {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting account ID {} for user {}: {}", id, (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }
}
