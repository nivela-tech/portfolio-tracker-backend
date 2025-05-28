package com.portfolio.tracker.service;

import com.portfolio.tracker.model.PortfolioEntry;
import com.portfolio.tracker.model.PortfolioAccount;
import com.portfolio.tracker.model.User;
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
import java.util.UUID; // Added import
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
        logger.debug("Validating entry data for user: {}", entry.getUser() != null ? entry.getUser().getEmail() : "null");
        if (entry.getType() == null) {
            logger.warn("Validation failed: Entry type is null");
            throw new IllegalArgumentException("Entry type cannot be null");
        }
        if (entry.getAccountId() == null && entry.getAccount() == null) {
            logger.warn("Validation failed: Account ID or Account object must be present");
            throw new IllegalArgumentException("Account ID or Account object must be present");
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
        if (entry.getUser() == null) {
            logger.warn("Validation failed: Entry must be associated with a user");
            throw new IllegalArgumentException("Entry must have an associated user.");
        }
        logger.debug("Entry validation successful for user: {}", entry.getUser().getEmail());
    }

    public PortfolioEntry addEntry(PortfolioEntry entry, User user) {
        logger.info("Adding new portfolio entry for account ID: {} by user: {}", entry.getAccountId(), user.getEmail());
        try {
            entry.setUser(user);
            if (entry.getAccountId() != null) {
                PortfolioAccount account = accountService.getAccountByIdAndUser(entry.getAccountId(), user);
                entry.setAccount(account);
            } else if (entry.getAccount() != null && entry.getAccount().getId() != null) {
                PortfolioAccount account = accountService.getAccountByIdAndUser(entry.getAccount().getId(), user);
                entry.setAccount(account);
                entry.setAccountId(account.getId());
            } else {
                logger.warn("Account ID or a valid Account object must be provided for user: {}", user.getEmail());
                throw new IllegalArgumentException("Account ID or a valid Account object must be provided.");
            }
            validateEntry(entry);
            PortfolioEntry savedEntry = portfolioRepository.save(entry);
            logger.info("Successfully added entry with ID: {} for user: {}", savedEntry.getId(), user.getEmail());
            return savedEntry;
        } catch (EntityNotFoundException enfe) {
            logger.warn("Failed to add entry for user {}: Account not found or not accessible by user. Details: {}", user.getEmail(), enfe.getMessage());
            throw enfe;
        } catch (Exception e) {
            logger.error("Failed to add portfolio entry for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public PortfolioEntry updateEntry(PortfolioEntry entryFromRequest, User user) {
        logger.info("Updating portfolio entry with ID: {} by user: {}", entryFromRequest.getId(), user.getEmail());
        if (entryFromRequest.getId() == null) {
            logger.warn("Update failed: Entry ID is null for user: {}", user.getEmail());
            throw new IllegalArgumentException("Entry ID cannot be null for an update operation.");
        }
        try {
            PortfolioEntry existingEntry = portfolioRepository.findByIdAndUser(entryFromRequest.getId(), user)
                .orElseThrow(() -> {
                    logger.warn("Entry not found with ID: {} for user: {}", entryFromRequest.getId(), user.getEmail());
                    return new EntityNotFoundException("Entry not found with ID: " + entryFromRequest.getId() + " for user " + user.getEmail());
                });

            existingEntry.setType(entryFromRequest.getType());
            existingEntry.setSource(entryFromRequest.getSource());
            existingEntry.setAmount(entryFromRequest.getAmount());
            existingEntry.setCurrency(entryFromRequest.getCurrency());
            existingEntry.setCountry(entryFromRequest.getCountry());
            existingEntry.setNotes(entryFromRequest.getNotes());

            UUID requestedAccountId = entryFromRequest.getAccountId();
            if (entryFromRequest.getAccount() != null && entryFromRequest.getAccount().getId() != null) {
                requestedAccountId = entryFromRequest.getAccount().getId();
            }

            if (requestedAccountId != null &&
                (existingEntry.getAccount() == null || !requestedAccountId.equals(existingEntry.getAccount().getId()))) {
                PortfolioAccount newAccount = accountService.getAccountByIdAndUser(requestedAccountId, user);
                existingEntry.setAccount(newAccount);
            }
            
            if (existingEntry.getAccount() != null) {
                existingEntry.setAccountId(existingEntry.getAccount().getId());
            } else {
                 logger.error("Critical: Entry ID {} (User: {}) is not associated with an account after update attempt.", existingEntry.getId(), user.getEmail());
                throw new IllegalStateException("Entry must be associated with an account.");
            }
            
            validateEntry(existingEntry);
            PortfolioEntry updatedEntry = portfolioRepository.save(existingEntry);
            logger.info("Successfully updated entry with ID: {} for user: {}", updatedEntry.getId(), user.getEmail());
            return updatedEntry;
        } catch (EntityNotFoundException enfe) {
            logger.warn("Failed to update entry for user {}: {}. Details: {}", user.getEmail(), entryFromRequest.getId(), enfe.getMessage());
            throw enfe;
        } catch (Exception e) {
            logger.error("Failed to update portfolio entry ID {} for user {}: {}", entryFromRequest.getId(), user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public void deleteEntry(UUID id, User user) { // Changed Long to UUID
        logger.info("Deleting portfolio entry with ID: {} by user: {}", id, user.getEmail());
        try {
            PortfolioEntry entry = portfolioRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    logger.warn("Entry not found with ID: {} for user: {} during delete attempt.", id, user.getEmail());
                    return new EntityNotFoundException("Entry not found with ID: " + id + " for user " + user.getEmail());
                });
            portfolioRepository.delete(entry);
            logger.info("Successfully deleted entry with ID: {} for user: {}", id, user.getEmail());
        } catch (EntityNotFoundException enfe) {
            logger.warn("Failed to delete entry for user {}: Entry ID {} not found or not accessible. Details: {}", user.getEmail(), id, enfe.getMessage());
            throw enfe;
        } catch (Exception e) {
            logger.error("Failed to delete portfolio entry ID {} for user {}: {}", id, user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public PortfolioEntry getEntryByIdAndUser(UUID id, User user) { // Changed Long to UUID
        logger.debug("Fetching entry with ID: {} for user: {}", id, user.getEmail());
        try {
            PortfolioEntry entry = portfolioRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    logger.warn("Entry not found with ID: {} for user: {}", id, user.getEmail());
                    return new EntityNotFoundException("Entry not found with ID: " + id + " for user " + user.getEmail());
                });
            logger.debug("Successfully fetched entry with ID: {} for user: {}", id, user.getEmail());
            return entry;
        } catch (EntityNotFoundException enfe) {
            logger.warn("Failed to fetch entry for user {}: Entry ID {} not found or not accessible. Details: {}", user.getEmail(), id, enfe.getMessage());
            throw enfe;
        } catch (Exception e) {
            logger.error("Failed to fetch portfolio entry ID {} for user {}: {}", id, user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public List<PortfolioEntry> getAllEntriesByUser(User user) {
        logger.debug("Fetching all entries for user: {}", user.getEmail());
        try {
            List<PortfolioEntry> entries = portfolioRepository.findByUser(user);
            logger.debug("Successfully fetched {} entries for user: {}", entries.size(), user.getEmail());
            return entries;
        } catch (Exception e) {
            logger.error("Failed to fetch all portfolio entries for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public List<PortfolioEntry> getEntriesByAccountIdAndUser(UUID accountId, User user) { // Changed Long to UUID
        logger.debug("Fetching entries for account ID: {} by user: {}", accountId, user.getEmail());
        // First, verify the account belongs to the user to ensure data privacy and correctness
        accountService.getAccountByIdAndUser(accountId, user); // This will throw EntityNotFoundException if not found/accessible
        try {
            PortfolioAccount account = accountService.getAccountByIdAndUser(accountId, user);
            List<PortfolioEntry> entries = portfolioRepository.findByAccountAndUser(account, user);
            logger.debug("Successfully fetched {} entries for account ID: {} by user: {}", entries.size(), accountId, user.getEmail());
            return entries;
        } catch (Exception e) {
            logger.error("Failed to fetch portfolio entries for account ID {} and user {}: {}", accountId, user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public Map<EntryType, BigDecimal> getPortfolioSummaryByUser(User user) {
        logger.debug("Generating portfolio summary for user: {}", user.getEmail());
        try {
            List<PortfolioEntry> entries = portfolioRepository.findByUser(user);
            Map<EntryType, BigDecimal> summary = entries.stream()
                .collect(Collectors.groupingBy(
                    PortfolioEntry::getType,
                    Collectors.mapping(PortfolioEntry::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
            logger.debug("Successfully generated portfolio summary for user: {}", user.getEmail());
            return summary;
        } catch (Exception e) {
            logger.error("Failed to generate portfolio summary for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public Map<String, Map<EntryType, BigDecimal>> getPortfolioSummaryByAccountAndUser(User user) {
        logger.debug("Generating portfolio summary by account for user: {}", user.getEmail());
        try {
            List<PortfolioAccount> accounts = accountService.getAllAccountsByUser(user);
            Map<UUID, String> accountIdToNameMap = accounts.stream()
                .collect(Collectors.toMap(PortfolioAccount::getId, PortfolioAccount::getName)); // Corrected: getAccountName to getName

            List<PortfolioEntry> entries = portfolioRepository.findByUser(user);
            Map<String, Map<EntryType, BigDecimal>> summaryByAccount = entries.stream()
                .filter(entry -> entry.getAccount() != null && accountIdToNameMap.containsKey(entry.getAccount().getId()))
                .collect(Collectors.groupingBy(
                    entry -> accountIdToNameMap.get(entry.getAccount().getId()), // Group by account name
                    Collectors.groupingBy(
                        PortfolioEntry::getType,
                        Collectors.mapping(PortfolioEntry::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                    )
                ));
            logger.debug("Successfully generated portfolio summary by account for user: {}", user.getEmail());
            return summaryByAccount;
        } catch (Exception e) {
            logger.error("Failed to generate portfolio summary by account for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public BigDecimal getTotalPortfolioValueByUser(User user) {
        logger.debug("Calculating total portfolio value for user: {}", user.getEmail());
        try {
            List<PortfolioEntry> entries = portfolioRepository.findByUser(user);
            BigDecimal totalValue = entries.stream()
                .map(PortfolioEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            logger.debug("Successfully calculated total portfolio value for user {}: {}", user.getEmail(), totalValue);
            return totalValue;
        } catch (Exception e) {
            logger.error("Failed to calculate total portfolio value for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
    
    public Map<String, BigDecimal> getPortfolioDistributionByCountry(User user) {
        logger.debug("Generating portfolio distribution by country for user: {}", user.getEmail());
        try {
            List<PortfolioEntry> entries = portfolioRepository.findByUser(user);
            Map<String, BigDecimal> distribution = entries.stream()
                .collect(Collectors.groupingBy(
                    PortfolioEntry::getCountry,
                    Collectors.mapping(PortfolioEntry::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
            logger.debug("Successfully generated portfolio distribution by country for user: {}", user.getEmail());
            return distribution;
        } catch (Exception e) {
            logger.error("Failed to generate portfolio distribution by country for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public Map<String, BigDecimal> getPortfolioDistributionByCurrency(User user) {
        logger.debug("Generating portfolio distribution by currency for user: {}", user.getEmail());
        try {
            List<PortfolioEntry> entries = portfolioRepository.findByUser(user);
            Map<String, BigDecimal> distribution = entries.stream()
                .collect(Collectors.groupingBy(
                    PortfolioEntry::getCurrency,
                    Collectors.mapping(PortfolioEntry::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
            logger.debug("Successfully generated portfolio distribution by currency for user: {}", user.getEmail());
            return distribution;
        } catch (Exception e) {
            logger.error("Failed to generate portfolio distribution by currency for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public List<PortfolioEntry> getCombinedEntriesByUser(User user) {
        logger.debug("Fetching combined entries for user: {}", user.getEmail());
        return portfolioRepository.findByUser(user);
    }

    public Map<String, BigDecimal> getCombinedEntriesByCurrencyAndUser(User user) {
        logger.debug("Fetching portfolio grouped by currency for user: {}", user.getEmail());
        return portfolioRepository.findByUser(user).stream()
            .collect(Collectors.groupingBy(
                PortfolioEntry::getCurrency,
                Collectors.mapping(PortfolioEntry::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));
    }

    public Map<String, BigDecimal> getCombinedEntriesByCountryAndUser(User user) {
        logger.debug("Fetching portfolio grouped by country for user: {}", user.getEmail());
        return portfolioRepository.findByUser(user).stream()
            .collect(Collectors.groupingBy(
                PortfolioEntry::getCountry,
                Collectors.mapping(PortfolioEntry::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));
    }

    public Map<String, BigDecimal> getCombinedEntriesBySourceAndUser(User user) {
        logger.debug("Fetching portfolio grouped by source for user: {}", user.getEmail());
        return portfolioRepository.findByUser(user).stream()
            .collect(Collectors.groupingBy(
                PortfolioEntry::getSource,
                Collectors.mapping(PortfolioEntry::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));
    }

    public Map<String, BigDecimal> getCombinedEntriesByTypeAndUser(User user) {
        logger.debug("Fetching portfolio grouped by type for user: {}", user.getEmail());
        return portfolioRepository.findByUser(user).stream()
            .collect(Collectors.groupingBy(
                entry -> entry.getType().toString(),
                Collectors.mapping(PortfolioEntry::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));
    }

    public List<PortfolioEntry> getEntriesByCurrencyAndAccountIdAndUser(String currency, UUID accountId, User user) {
        logger.debug("Fetching entries by currency: {} and account ID: {} for user: {}", currency, accountId, user.getEmail());
        PortfolioAccount account = accountService.getAccountByIdAndUser(accountId, user);
        return portfolioRepository.findByAccountAndUser(account, user).stream()
            .filter(entry -> currency.equals(entry.getCurrency()))
            .collect(Collectors.toList());
    }

    public List<PortfolioEntry> getEntriesByCurrencyAndUser(String currency, User user) {
        logger.debug("Fetching entries by currency: {} for user: {}", currency, user.getEmail());
        return portfolioRepository.findByUser(user).stream()
            .filter(entry -> currency.equals(entry.getCurrency()))
            .collect(Collectors.toList());
    }

    public List<PortfolioEntry> getEntriesByCountryAndAccountIdAndUser(String country, UUID accountId, User user) {
        logger.debug("Fetching entries by country: {} and account ID: {} for user: {}", country, accountId, user.getEmail());
        PortfolioAccount account = accountService.getAccountByIdAndUser(accountId, user);
        return portfolioRepository.findByAccountAndUser(account, user).stream()
            .filter(entry -> country.equals(entry.getCountry()))
            .collect(Collectors.toList());
    }

    public List<PortfolioEntry> getEntriesByCountryAndUser(String country, User user) {
        logger.debug("Fetching entries by country: {} for user: {}", country, user.getEmail());
        return portfolioRepository.findByUser(user).stream()
            .filter(entry -> country.equals(entry.getCountry()))
            .collect(Collectors.toList());
    }

    public List<PortfolioEntry> getEntriesBySourceAndAccountIdAndUser(String source, UUID accountId, User user) {
        logger.debug("Fetching entries by source: {} and account ID: {} for user: {}", source, accountId, user.getEmail());
        PortfolioAccount account = accountService.getAccountByIdAndUser(accountId, user);
        return portfolioRepository.findByAccountAndUser(account, user).stream()
            .filter(entry -> source.equals(entry.getSource()))
            .collect(Collectors.toList());
    }

    public List<PortfolioEntry> getEntriesBySourceAndUser(String source, User user) {
        logger.debug("Fetching entries by source: {} for user: {}", source, user.getEmail());
        return portfolioRepository.findByUser(user).stream()
            .filter(entry -> source.equals(entry.getSource()))
            .collect(Collectors.toList());
    }

    public List<PortfolioEntry> getEntriesByTypeAndAccountIdAndUser(EntryType type, UUID accountId, User user) {
        logger.debug("Fetching entries by type: {} and account ID: {} for user: {}", type, accountId, user.getEmail());
        PortfolioAccount account = accountService.getAccountByIdAndUser(accountId, user);
        return portfolioRepository.findByAccountAndUser(account, user).stream()
            .filter(entry -> type.equals(entry.getType()))
            .collect(Collectors.toList());
    }

    public List<PortfolioEntry> getEntriesByTypeAndUser(EntryType type, User user) {
        logger.debug("Fetching entries by type: {} for user: {}", type, user.getEmail());
        return portfolioRepository.findByUser(user).stream()
            .filter(entry -> type.equals(entry.getType()))
            .collect(Collectors.toList());
    }
}
