package com.portfolio.tracker.controller;

import com.portfolio.tracker.model.PortfolioAccount;
import com.portfolio.tracker.service.PortfolioAccountService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE
})
public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private PortfolioAccountService accountService;

    @PostMapping
    public ResponseEntity<PortfolioAccount> createAccount(@RequestBody PortfolioAccount account) {
        logger.info("Received request to create account: {}", account.getName());
        try {
            PortfolioAccount createdAccount = accountService.createAccount(account);
            logger.info("Successfully created account with ID: {}", createdAccount.getId());
            return ResponseEntity.ok(createdAccount);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid account data received: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating account: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<PortfolioAccount>> getAllAccounts() {
        logger.info("Received request to get all accounts");
        try {
            List<PortfolioAccount> accounts = accountService.getAllAccounts();
            logger.info("Retrieved {} accounts", accounts.size());
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            logger.error("Error retrieving all accounts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioAccount> getAccountById(@PathVariable Long id) {
        logger.info("Received request to get account with ID: {}", id);
        try {
            PortfolioAccount account = accountService.getAccountById(id);
            logger.info("Successfully retrieved account: {}", account.getName());
            return ResponseEntity.ok(account);
        } catch (EntityNotFoundException e) {
            logger.warn("Account not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid account ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error retrieving account with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        logger.error("Unhandled exception occurred: {}", e.getMessage(), e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("An error occurred: " + e.getMessage());
    }
}
