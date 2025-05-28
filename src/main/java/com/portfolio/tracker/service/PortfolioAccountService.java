package com.portfolio.tracker.service;

import com.portfolio.tracker.model.PortfolioAccount;
import com.portfolio.tracker.model.User;
import com.portfolio.tracker.repository.PortfolioAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // Added for UUID
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class PortfolioAccountService {
    
    private static final Logger logger = LoggerFactory.getLogger(PortfolioAccountService.class);
    
    @Autowired
    private PortfolioAccountRepository accountRepository;

    public PortfolioAccount createAccount(PortfolioAccount account, User user) { // Added User parameter
        logger.info("Attempting to create new account: {} for user: {}", account.getName(), user.getEmail());
        try {
            account.setUser(user); // Associate account with the user
            validateAccount(account);
            PortfolioAccount savedAccount = accountRepository.save(account);
            logger.info("Successfully created account with ID: {} and name: {} for user: {}", savedAccount.getId(), savedAccount.getName(), user.getEmail());
            return savedAccount;
        } catch (Exception e) {
            logger.error("Failed to create account for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    private void validateAccount(PortfolioAccount account) {
        logger.debug("Validating account data");
        if (account.getName() == null || account.getName().trim().isEmpty()) {
            logger.warn("Validation failed: Account name is empty");
            throw new IllegalArgumentException("Account name cannot be empty");
        }
        if (account.getRelationship() == null || account.getRelationship().trim().isEmpty()) {
            logger.warn("Validation failed: Relationship is empty");
            throw new IllegalArgumentException("Relationship cannot be empty");
        }
        if (account.getUser() == null) { // Added user validation
            logger.warn("Validation failed: Account must be associated with a user");
            throw new IllegalArgumentException("Account must have an associated user.");
        }
        logger.debug("Account validation successful");
    }

    @Transactional(readOnly = true)
    public List<PortfolioAccount> getAllAccountsByUser(User user) { // Renamed and added User parameter
        logger.info("Fetching all accounts for user: {}", user.getEmail());
        try {
            List<PortfolioAccount> accounts = accountRepository.findByUser(user);
            accounts.forEach(account -> {
                Hibernate.initialize(account.getEntries()); // Keep if entries are needed immediately
                logger.debug("Initialized entries for account ID: {}, User: {}, Entry count: {}", 
                    account.getId(), user.getEmail(), account.getEntries().size());
            });
            logger.info("Successfully retrieved and initialized {} accounts for user: {}", accounts.size(), user.getEmail());
            return accounts;
        } catch (Exception e) {
            logger.error("Error fetching all accounts for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public PortfolioAccount getAccountByIdAndUser(UUID id, User user) { // Changed Long to UUID
        logger.info("Fetching account with ID: {} for user: {}", id, user.getEmail());
        if (id == null) {
            logger.warn("Null ID provided for account lookup by user: {}", user.getEmail());
            throw new IllegalArgumentException("Account ID cannot be null");
        }

        try {
            Optional<PortfolioAccount> accountOpt = accountRepository.findByIdAndUser(id, user);
            if (accountOpt.isPresent()) {
                PortfolioAccount account = accountOpt.get();
                Hibernate.initialize(account.getEntries());
                logger.info("Successfully retrieved and initialized account - ID: {}, Name: {}, User: {}, Entries: {}", 
                    account.getId(), account.getName(), user.getEmail(), account.getEntries().size());
                return account;
            } else {
                logger.warn("Account not found with ID: {} for user: {}", id, user.getEmail());
                throw new EntityNotFoundException("Account not found with ID: " + id + " for user: " + user.getEmail());
            }
        } catch (Exception e) {
            logger.error("Error fetching account with ID {} for user {}: {}", id, user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public PortfolioAccount updateAccount(UUID id, PortfolioAccount accountDetails, User user) { // Changed Long to UUID
        logger.info("Attempting to update account with ID: {} for user: {}", id, user.getEmail());
        try {
            PortfolioAccount existingAccount = getAccountByIdAndUser(id, user); 
            logger.debug("Found existing account: {} for user: {}", existingAccount.getName(), user.getEmail());
            
            PortfolioAccount tempValidationAccount = new PortfolioAccount();
            tempValidationAccount.setName(accountDetails.getName());
            tempValidationAccount.setRelationship(accountDetails.getRelationship());
            tempValidationAccount.setUser(user); 
            validateAccount(tempValidationAccount);

            existingAccount.setName(accountDetails.getName());
            existingAccount.setRelationship(accountDetails.getRelationship());

            PortfolioAccount updatedAccount = accountRepository.save(existingAccount);
            Hibernate.initialize(updatedAccount.getEntries()); 
            logger.info("Successfully updated account - ID: {}, Name: {} for user: {}", id, updatedAccount.getName(), user.getEmail());
            return updatedAccount;
        } catch (Exception e) {
            logger.error("Failed to update account with ID {} for user {}: {}", id, user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
    
    public void deleteAccount(UUID id, User user) { // Changed Long to UUID
        logger.info("Attempting to delete account with ID: {} by user: {}", id, user.getEmail());
        try {
            PortfolioAccount account = getAccountByIdAndUser(id, user); 
            logger.debug("Found account to delete: {} for user: {}", account.getName(), user.getEmail());
            
            if (account.getEntries() != null && !account.getEntries().isEmpty()) {
                logger.info("Account ID: {} will be deleted along with {} associated entries", 
                    id, account.getEntries().size());
            }

            accountRepository.delete(account);
            logger.info("Successfully deleted account with ID: {} and Name: {} for user: {}", 
                id, account.getName(), user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to delete account with ID {} for user {}: {}", 
                id, user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
}
