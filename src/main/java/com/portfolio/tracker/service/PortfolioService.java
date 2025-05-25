package com.portfolio.tracker.service;

import com.portfolio.tracker.model.PortfolioEntry;
import com.portfolio.tracker.model.PortfolioAccount;
import com.portfolio.tracker.repository.PortfolioRepository;
import com.portfolio.tracker.model.EntryType;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
@Transactional
public class PortfolioService {
    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);
    
    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PortfolioAccountService accountService;

    private void validateEntry(PortfolioEntry entry) {
        logger.debug("Validating entry data");
        if (entry.getType() == null) {
            logger.warn("Validation failed: Entry type is null");
            throw new IllegalArgumentException("Entry type cannot be null");
        }
        if (entry.getAccountId() == null) {
            logger.warn("Validation failed: Account ID is null");
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        if (entry.getAmount() == null || entry.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Validation failed: Invalid amount");
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (entry.getCurrency() == null || entry.getCurrency().trim().isEmpty()) {
            logger.warn("Validation failed: Currency is empty");
            throw new IllegalArgumentException("Currency cannot be empty");
        }
        if (entry.getCountry() == null || entry.getCountry().trim().isEmpty()) {
            logger.warn("Validation failed: Country is empty");
            throw new IllegalArgumentException("Country cannot be empty");
        }
        logger.debug("Entry validation successful");
    }

    public PortfolioEntry addEntry(PortfolioEntry entry) {
        logger.info("Adding new portfolio entry for account ID: {}", entry.getAccountId());
        try {
            // Ensure accountId is set from account object if account is present
            if (entry.getAccount() != null && entry.getAccountId() == null) {
                entry.setAccountId(entry.getAccount().getId());
            } else if (entry.getAccount() == null && entry.getAccountId() != null) {
                // If accountId is provided but account object is not, fetch and set account
                PortfolioAccount account = accountService.getAccountById(entry.getAccountId());
                entry.setAccount(account); // This will also set the transient accountId via custom setter
            }

            validateEntry(entry);
            // Account should be set by now if accountId was valid
            if (entry.getAccount() == null && entry.getAccountId() != null) {
                 // This case should ideally be caught by validateEntry if accountId implies a non-existent account
                 // or handled by ensuring account is fetched and set before validation.
                 // For robustness, re-fetch if somehow account is null but accountId is not.
                PortfolioAccount account = accountService.getAccountById(entry.getAccountId());
                entry.setAccount(account);
            }
            
            PortfolioEntry savedEntry = portfolioRepository.save(entry);
            logger.info("Successfully added entry with ID: {}", savedEntry.getId());
            return savedEntry;
        } catch (Exception e) {
            logger.error("Failed to add portfolio entry: {}", e.getMessage(), e);
            throw e;
        }
    }

    public PortfolioEntry updateEntry(PortfolioEntry entryFromRequest) {
        logger.info("Updating portfolio entry with ID: {}", entryFromRequest.getId());
        try {
            PortfolioEntry existingEntry = portfolioRepository.findById(entryFromRequest.getId())
                .orElseThrow(() -> {
                    logger.warn("Entry not found with ID: {}", entryFromRequest.getId());
                    return new EntityNotFoundException("Entry not found with ID: " + entryFromRequest.getId());
                });

            // Update mutable fields of existingEntry from entryFromRequest
            if (entryFromRequest.getType() != null) {
                existingEntry.setType(entryFromRequest.getType());
            }
            if (entryFromRequest.getSource() != null) {
                existingEntry.setSource(entryFromRequest.getSource());
            }
            if (entryFromRequest.getAmount() != null) {
                existingEntry.setAmount(entryFromRequest.getAmount());
            }
            if (entryFromRequest.getCurrency() != null) {
                existingEntry.setCurrency(entryFromRequest.getCurrency());
            }
            if (entryFromRequest.getCountry() != null) {
                existingEntry.setCountry(entryFromRequest.getCountry());
            }
            // dateAdded is typically not updated this way.

            // Handle account change if accountId is provided in the request
            if (entryFromRequest.getAccountId() != null &&
                (existingEntry.getAccount() == null || !entryFromRequest.getAccountId().equals(existingEntry.getAccount().getId()))) {
                PortfolioAccount newAccount = accountService.getAccountById(entryFromRequest.getAccountId());
                existingEntry.setAccount(newAccount); // Custom setter updates transient accountId
            }

            // Ensure the transient accountId on existingEntry is synchronized before validation
            if (existingEntry.getAccount() != null) {
                existingEntry.setAccountId(existingEntry.getAccount().getId());
            } else {
                // This case should ideally not happen if account_id is non-nullable in DB
                // and an entry always has an account.
                existingEntry.setAccountId(null);
            }
            
            validateEntry(existingEntry); // Validate the updated existingEntry
            
            PortfolioEntry updatedEntry = portfolioRepository.save(existingEntry);
            logger.info("Successfully updated entry with ID: {}", updatedEntry.getId());
            return updatedEntry;
        } catch (Exception e) {
            logger.error("Failed to update portfolio entry: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void deleteEntry(Long id) {
        logger.info("Deleting portfolio entry with ID: {}", id);
        try {
            if (!portfolioRepository.existsById(id)) {
                logger.warn("Entry not found with ID: {}", id);
                throw new EntityNotFoundException("Entry not found with ID: " + id);
            }
            
            portfolioRepository.deleteById(id);
            logger.info("Successfully deleted entry with ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete portfolio entry: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<PortfolioEntry> getAllEntries() {
        logger.info("Fetching all portfolio entries");
        try {
            List<PortfolioEntry> entries = portfolioRepository.findAll();
            logger.info("Retrieved {} entries", entries.size());
            return entries;
        } catch (Exception e) {
            logger.error("Failed to fetch all entries: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<PortfolioEntry> getEntriesByAccount(Long accountId) {
        logger.info("Fetching entries for account ID: {}", accountId);
        try {            List<PortfolioEntry> entries = portfolioRepository.findByAccount_IdOrderByDateAddedDesc(accountId);
            logger.info("Retrieved {} entries for account {}", entries.size(), accountId);
            return entries;
        } catch (Exception e) {
            logger.error("Failed to fetch entries for account {}: {}", accountId, e.getMessage(), e);
            throw e;
        }
    }

    public List<PortfolioEntry> getEntriesByCurrency(String currency) {
        return portfolioRepository.findByCurrency(currency);
    }

    public List<PortfolioEntry> getEntriesByCurrencyAndAccount(String currency, Long accountId) {
        return portfolioRepository.findByCurrencyAndAccount_Id(currency, accountId);
    }

    public List<PortfolioEntry> getEntriesByCountry(String country) {
        return portfolioRepository.findByCountry(country);
    }

    public List<PortfolioEntry> getEntriesByCountryAndAccount(String country, Long accountId) {
        return portfolioRepository.findByCountryAndAccount_Id(country, accountId);
    }

    public List<PortfolioEntry> getEntriesBySource(String source) {
        return portfolioRepository.findBySource(source);
    }

    public List<PortfolioEntry> getEntriesBySourceAndAccount(String source, Long accountId) {
        return portfolioRepository.findBySourceAndAccount_Id(source, accountId);
    }    public List<PortfolioEntry> getCombinedPortfolioEntries() {
        logger.info("Fetching all portfolio entries with account details");
        try {
            List<PortfolioEntry> entries = portfolioRepository.findAllByOrderByDateAddedDesc();
            // Ensure account data is loaded for each entry
            for (PortfolioEntry entry : entries) {
                if (entry.getAccount() != null) {
                    // Access the name to ensure it's loaded
                    entry.getAccount().getName();
                }
            }
            logger.info("Retrieved {} entries with account details", entries.size());
            return entries;
        } catch (Exception e) {
            logger.error("Failed to fetch entries with account details: {}", e.getMessage(), e);
            throw e;
        }
    }    public Map<String, BigDecimal> getCombinedPortfolioByCurrency() {
        List<PortfolioEntry> entries = getAllEntries();
        return entries.stream()
            .collect(Collectors.groupingBy(
                PortfolioEntry::getCurrency,
                Collectors.mapping(
                    PortfolioEntry::getAmount,
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Function.identity(),
                        BigDecimal::add
                    )
                )
            ));
    }

    public Map<String, BigDecimal> getCombinedPortfolioByCountry() {
        List<PortfolioEntry> entries = getAllEntries();
        return entries.stream()
            .collect(Collectors.groupingBy(
                PortfolioEntry::getCountry,
                Collectors.mapping(
                    PortfolioEntry::getAmount,
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Function.identity(),
                        BigDecimal::add
                    )
                )
            ));
    }

    public Map<String, BigDecimal> getCombinedPortfolioBySource() {
        List<PortfolioEntry> entries = getAllEntries();
        return entries.stream()
            .collect(Collectors.groupingBy(
                PortfolioEntry::getSource,
                Collectors.mapping(
                    PortfolioEntry::getAmount,
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Function.identity(),
                        BigDecimal::add
                    )
                )
            ));
    }

    public List<PortfolioEntry> getEntriesByType(EntryType type) {
        logger.info("Fetching entries by type: {}", type);
        return portfolioRepository.findByType(type);
    }

    public List<PortfolioEntry> getEntriesByTypeAndAccount(EntryType type, Long accountId) {
        logger.info("Fetching entries by type: {} and account ID: {}", type, accountId);
        return portfolioRepository.findByTypeAndAccount_Id(type, accountId);
    }

    public Map<String, BigDecimal> getCombinedPortfolioByType() {
        logger.info("Getting combined portfolio grouped by type");
        List<PortfolioEntry> entries = getAllEntries();
        return entries.stream()
            .collect(Collectors.groupingBy(
                entry -> entry.getType().name(),
                Collectors.mapping(
                    PortfolioEntry::getAmount,                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Function.identity(),
                        BigDecimal::add
                    )
                )
            ));
    }
}
