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
@RequestMapping("/api/accounts")
public class PortfolioAccountController {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioAccountController.class);

    @Autowired
    private PortfolioAccountService portfolioAccountService;

    @Autowired
    private UserService userService;

    private User getCurrentUser(OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            logger.warn("OAuth2User is null, cannot identify current user.");
            throw new IllegalStateException("User not authenticated");
        }
        User user = userService.getUserByEmail(oAuth2User.getAttribute("email"));
        if (user == null) {
            // This case should ideally be handled by CustomOAuth2UserService creating the user
            logger.error("User not found in database for email: {}", oAuth2User.getAttribute("email"));
            throw new EntityNotFoundException("User not found for email: " + oAuth2User.getAttribute("email"));
        }
        return user;
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody PortfolioAccount account, @AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            User currentUser = getCurrentUser(oAuth2User);
            logger.info("User {} creating account: {}", currentUser.getEmail(), account.getName());
            PortfolioAccount createdAccount = portfolioAccountService.createAccount(account, currentUser);
            return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid argument for creating account by user {}: {}", (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating account for user {}: {}", (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating account: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllAccounts(@AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            User currentUser = getCurrentUser(oAuth2User);
            logger.info("User {} fetching all their accounts", currentUser.getEmail());
            List<PortfolioAccount> accounts = portfolioAccountService.getAllAccountsByUser(currentUser);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            logger.error("Error fetching accounts for user {}: {}", (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching accounts: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable Long id, @AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            User currentUser = getCurrentUser(oAuth2User);
            logger.info("User {} fetching account by ID: {}", currentUser.getEmail(), id);
            PortfolioAccount account = portfolioAccountService.getAccountByIdAndUser(id, currentUser);
            return ResponseEntity.ok(account);
        } catch (EntityNotFoundException e) {
            logger.warn("Account not found with ID {} for user {}: {}", id, (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching account ID {} for user {}: {}", id, (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching account: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Long id, @RequestBody PortfolioAccount accountDetails, @AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            User currentUser = getCurrentUser(oAuth2User);
            logger.info("User {} updating account ID: {}", currentUser.getEmail(), id);
            PortfolioAccount updatedAccount = portfolioAccountService.updateAccount(id, accountDetails, currentUser);
            return ResponseEntity.ok(updatedAccount);
        } catch (EntityNotFoundException e) {
            logger.warn("Account not found for update with ID {} for user {}: {}", id, (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid argument for updating account ID {} by user {}: {}", id, (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating account ID {} for user {}: {}", id, (oAuth2User != null ? oAuth2_user.getAttribute("email") : "unknown"), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating account: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id, @AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            User currentUser = getCurrentUser(oAuth2User);
            logger.info("User {} deleting account ID: {}", currentUser.getEmail(), id);
            portfolioAccountService.deleteAccount(id, currentUser);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.warn("Account not found for deletion with ID {} for user {}: {}", id, (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) { // Catch specific exception for deletion conflicts (e.g., account has entries)
            logger.warn("Deletion conflict for account ID {} by user {}: {}", id, (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        catch (Exception e) {
            logger.error("Error deleting account ID {} for user {}: {}", id, (oAuth2User != null ? oAuth2User.getAttribute("email") : "unknown"), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting account: " + e.getMessage());
        }
    }
}
