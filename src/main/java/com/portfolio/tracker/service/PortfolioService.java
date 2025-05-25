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

            Long requestedAccountId = entryFromRequest.getAccountId();
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

    public void deleteEntry(Long id, User user) {
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
        }
         catch (Exception e) {
            logger.error("Failed to delete portfolio entry ID {} for user {}: {}", id, user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public List<PortfolioEntry> getAllEntriesByUser(User user) {
        logger.info("Fetching all portfolio entries for user: {}", user.getEmail());
        return portfolioRepository.findByUserOrderByDateAddedDesc(user);
    }

    public List<PortfolioEntry> getEntriesByAccountAndUser(Long accountId, User user) {
        logger.info("Fetching entries for account ID: {} for user: {}", accountId, user.getEmail());
        PortfolioAccount account = accountService.getAccountByIdAndUser(accountId, user); 
        return portfolioRepository.findByAccountAndUserOrderByDateAddedDesc(account, user);
    }

    public List<PortfolioEntry> getEntriesByCurrencyAndUser(String currency, User user) {
        logger.info("Fetching entries for currency: {} for user: {}", currency, user.getEmail());
        return portfolioRepository.findByCurrencyAndUser(currency, user);
    }

    public List<PortfolioEntry> getEntriesByCurrencyAndAccountAndUser(String currency, Long accountId, User user) {
        logger.info("Fetching entries for currency: {}, account ID: {} for user: {}", currency, accountId, user.getEmail());
        PortfolioAccount account = accountService.getAccountByIdAndUser(accountId, user);
        return portfolioRepository.findByCurrencyAndAccountAndUser(currency, account, user);
    }

    public List<PortfolioEntry> getEntriesByCountryAndUser(String country, User user) {
        logger.info("Fetching entries for country: {} for user: {}", country, user.getEmail());
        return portfolioRepository.findByCountryAndUser(country, user);
    }

    public List<PortfolioEntry> getEntriesByCountryAndAccountAndUser(String country, Long accountId, User user) {
        logger.info("Fetching entries for country: {}, account ID: {} for user: {}", country, accountId, user.getEmail());
        PortfolioAccount account = accountService.getAccountByIdAndUser(accountId, user);
        return portfolioRepository.findByCountryAndAccountAndUser(country, account, user);
    }

    public List<PortfolioEntry> getEntriesBySourceAndUser(String source, User user) {
        logger.info("Fetching entries for source: {} for user: {}", source, user.getEmail());
        return portfolioRepository.findBySourceAndUser(source, user);
    }

    public List<PortfolioEntry> getEntriesBySourceAndAccountAndUser(String source, Long accountId, User user) {
        logger.info("Fetching entries for source: {}, account ID: {} for user: {}", source, accountId, user.getEmail());
        PortfolioAccount account = accountService.getAccountByIdAndUser(accountId, user);
        return portfolioRepository.findBySourceAndAccountAndUser(source, account, user);
    }

    public List<PortfolioEntry> getEntriesByTypeAndUser(EntryType type, User user) {
        logger.info("Fetching entries for type: {} for user: {}", type, user.getEmail());
        return portfolioRepository.findByTypeAndUser(type, user);
    }

    public List<PortfolioEntry> getEntriesByTypeAndAccountAndUser(EntryType type, Long accountId, User user) {
        logger.info("Fetching entries for type: {}, account ID: {} for user: {}", type, accountId, user.getEmail());
        PortfolioAccount account = accountService.getAccountByIdAndUser(accountId, user);
        return portfolioRepository.findByTypeAndAccountAndUser(type, account, user);
    }

    @Transactional(readOnly = true)
    public List<PortfolioEntry> getCombinedPortfolioEntriesForUser(User user) {
        logger.info("Fetching combined portfolio entries for user: {}", user.getEmail());
        List<PortfolioEntry> allEntries = portfolioRepository.findByUserOrderByDateAddedDesc(user);
        Map<List<Object>, PortfolioEntry> combinedMap = allEntries.stream()
            .collect(Collectors.toMap(
                entry -> List.of(
                    entry.getSource() != null ? entry.getSource() : "N/A",
                    entry.getType(),
                    entry.getCurrency(),
                    entry.getCountry(),
                    entry.getAccount() != null ? entry.getAccount().getId() : -1L
                ),
                Function.identity(),
                (existing, replacement) -> {
                    existing.setAmount(existing.getAmount().add(replacement.getAmount()));
                    return existing;
                }
            ));
        List<PortfolioEntry> result = combinedMap.values().stream().collect(Collectors.toList());
        logger.info("Successfully combined {} entries into {} for user: {}", allEntries.size(), result.size(), user.getEmail());
        return result;
    }

    @Transactional(readOnly = true)
    public List<PortfolioEntry> getAllEntries() {
        logger.warn("Fetching all portfolio entries across all users. Consider for admin use only.");
        return portfolioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PortfolioEntry getEntryById(Long id) {
        logger.warn("Fetching entry by ID: {} across all users. Consider for admin use only.", id);
        return portfolioRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("Admin access: Entry not found with ID: {}", id);
                return new EntityNotFoundException("Entry not found with ID: " + id);
            });
    }
    
    @Transactional(readOnly = true)
    public List<PortfolioEntry> getEntriesByAccountId(Long accountId) {
        logger.warn("Fetching entries by account ID: {} across all users. Consider for admin use only.", accountId);
        return portfolioRepository.findByAccount_IdOrderByDateAddedDesc(accountId);
    }
}
