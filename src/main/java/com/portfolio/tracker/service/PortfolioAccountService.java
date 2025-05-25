package com.portfolio.tracker.service;

import com.portfolio.tracker.model.PortfolioAccount;
import com.portfolio.tracker.repository.PortfolioAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class PortfolioAccountService {
    
    private static final Logger logger = LoggerFactory.getLogger(PortfolioAccountService.class);
    
    @Autowired
    private PortfolioAccountRepository accountRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public PortfolioAccount createAccount(PortfolioAccount account) {
        logger.info("Attempting to create new account: {}", account.getName());
        try {
            validateAccount(account);
            PortfolioAccount savedAccount = accountRepository.save(account);
            logger.info("Successfully created account with ID: {} and name: {}", savedAccount.getId(), savedAccount.getName());
            return savedAccount;
        } catch (Exception e) {
            logger.error("Failed to create account: {}", e.getMessage(), e);
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
        logger.debug("Account validation successful");
    }

    @Transactional(readOnly = true)
    public List<PortfolioAccount> getAllAccounts() {
        logger.info("Fetching all accounts");
        try {
            List<PortfolioAccount> accounts = accountRepository.findAll();
            // Initialize the collections inside the transaction
            accounts.forEach(account -> {
                Hibernate.initialize(account.getEntries());
                logger.debug("Initialized entries for account ID: {}, Entry count: {}", 
                    account.getId(), account.getEntries().size());
            });
            logger.info("Successfully retrieved and initialized {} accounts", accounts.size());
            return accounts;
        } catch (Exception e) {
            logger.error("Error fetching all accounts: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public PortfolioAccount getAccountById(Long id) {
        logger.info("Fetching account with ID: {}", id);
        if (id == null) {
            logger.warn("Null ID provided for account lookup");
            throw new IllegalArgumentException("Account ID cannot be null");
        }

        try {
            Optional<PortfolioAccount> accountOpt = accountRepository.findById(id);
            if (accountOpt.isPresent()) {
                PortfolioAccount account = accountOpt.get();
                // Initialize the collections inside the transaction
                Hibernate.initialize(account.getEntries());
                logger.info("Successfully retrieved and initialized account - ID: {}, Name: {}, Entries: {}", 
                    account.getId(), account.getName(), account.getEntries().size());
                return account;
            } else {
                logger.warn("Account not found with ID: {}", id);
                throw new EntityNotFoundException("Account not found with ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Error fetching account with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public PortfolioAccount updateAccount(Long id, PortfolioAccount accountDetails) {
        logger.info("Attempting to update account with ID: {}", id);
        try {
            PortfolioAccount existingAccount = getAccountById(id);
            logger.debug("Found existing account: {}", existingAccount.getName());
            
            validateAccount(accountDetails);
            existingAccount.setName(accountDetails.getName());
            existingAccount.setRelationship(accountDetails.getRelationship());
            
            PortfolioAccount updatedAccount = accountRepository.save(existingAccount);
            // Initialize the collections before returning
            Hibernate.initialize(updatedAccount.getEntries());
            logger.info("Successfully updated account - ID: {}, Name: {}", id, updatedAccount.getName());
            return updatedAccount;
        } catch (Exception e) {
            logger.error("Failed to update account with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public void deleteAccount(Long id) {
        logger.info("Attempting to delete account with ID: {}", id);
        try {
            PortfolioAccount account = getAccountById(id);
            logger.debug("Found account to delete: {}", account.getName());
            accountRepository.delete(account);
            logger.info("Successfully deleted account with ID: {} and Name: {}", id, account.getName());
        } catch (Exception e) {
            logger.error("Failed to delete account with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
