package com.portfolio.tracker.controller;

import com.portfolio.tracker.model.PortfolioEntry;
import com.portfolio.tracker.model.EntryType;
import com.portfolio.tracker.model.User;
import com.portfolio.tracker.service.PortfolioService;
import com.portfolio.tracker.service.ExportService;
import com.portfolio.tracker.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE
})
public class PortfolioController {
    private static final Logger logger = LoggerFactory.getLogger(PortfolioController.class);

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private UserService userService;

    private User getCurrentUser(OAuth2User principal) {
        if (principal == null) {
            return null;
        }
        String providerId = principal.getAttribute("sub"); // Or "name" depending on provider and CustomOAuth2UserService
        return userService.findByProviderId(providerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with provider ID: " + providerId));
    }

    @PostMapping
    public ResponseEntity<PortfolioEntry> addEntry(@RequestBody PortfolioEntry entry, @AuthenticationPrincipal OAuth2User principal) {
        logger.info("Received request to add new portfolio entry");
        try {
            User currentUser = getCurrentUser(principal);
            PortfolioEntry savedEntry = portfolioService.addEntry(entry, currentUser);
            logger.info("Successfully added entry with ID: {} for user: {}", savedEntry.getId(), currentUser.getEmail());
            return ResponseEntity.ok(savedEntry);
        } catch (Exception e) {
            logger.error("Error adding portfolio entry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortfolioEntry> updateEntry(@PathVariable Long id, @RequestBody PortfolioEntry entry, @AuthenticationPrincipal OAuth2User principal) {
        logger.info("Received request to update portfolio entry with ID: {}", id);
        try {
            User currentUser = getCurrentUser(principal);
            entry.setId(id);
            PortfolioEntry updatedEntry = portfolioService.updateEntry(entry, currentUser);
            logger.info("Successfully updated entry with ID: {} for user: {}", updatedEntry.getId(), currentUser.getEmail());
            return ResponseEntity.ok(updatedEntry);
        } catch (EntityNotFoundException e) {
            logger.warn("Entry not found with ID: {} or user not authorized", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating portfolio entry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id, @AuthenticationPrincipal OAuth2User principal) {
        logger.info("Received request to delete portfolio entry with ID: {}", id);
        try {
            User currentUser = getCurrentUser(principal);
            portfolioService.deleteEntry(id, currentUser);
            logger.info("Successfully deleted entry with ID: {} for user: {}", id, currentUser.getEmail());
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            logger.warn("Entry not found with ID: {} or user not authorized", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting portfolio entry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<PortfolioEntry>> getAllEntries(@RequestParam(required = false) Long accountId, @AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        logger.info("Received request to get all entries for user: {}" + (accountId != null ? " for account " + accountId : ""), currentUser.getEmail());
        try {
            List<PortfolioEntry> entries;
            if (accountId != null) {
                entries = portfolioService.getEntriesByAccountAndUser(accountId, currentUser);
            } else {
                entries = portfolioService.getAllEntriesByUser(currentUser);
            }
            logger.info("Successfully retrieved {} entries for user: {}", entries.size(), currentUser.getEmail());
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Error retrieving portfolio entries for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/combined")
    public ResponseEntity<List<PortfolioEntry>> getCombinedPortfolio(@AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        logger.info("Received request to get combined portfolio for user: {}", currentUser.getEmail());
        try {
            List<PortfolioEntry> entries = portfolioService.getCombinedPortfolioEntriesByUser(currentUser);
            logger.info("Successfully retrieved combined portfolio with {} entries for user: {}", entries.size(), currentUser.getEmail());
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Error retrieving combined portfolio for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/combined/by-currency")
    public ResponseEntity<Map<String, BigDecimal>> getCombinedPortfolioByCurrency(@AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        logger.info("Received request to get portfolio grouped by currency for user: {}", currentUser.getEmail());
        try {
            Map<String, BigDecimal> data = portfolioService.getCombinedPortfolioByCurrencyUser(currentUser);
            logger.info("Successfully retrieved portfolio data for {} currencies for user: {}", data.size(), currentUser.getEmail());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("Error retrieving portfolio by currency for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/combined/by-country")
    public ResponseEntity<Map<String, BigDecimal>> getCombinedPortfolioByCountry(@AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        logger.info("Received request to get portfolio grouped by country for user: {}", currentUser.getEmail());
        try {
            Map<String, BigDecimal> data = portfolioService.getCombinedPortfolioByCountryUser(currentUser);
            logger.info("Successfully retrieved portfolio data for {} countries for user: {}", data.size(), currentUser.getEmail());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("Error retrieving portfolio by country for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/combined/by-source")
    public ResponseEntity<Map<String, BigDecimal>> getCombinedPortfolioBySource(@AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        logger.info("Received request to get portfolio grouped by source for user: {}", currentUser.getEmail());
        try {
            Map<String, BigDecimal> data = portfolioService.getCombinedPortfolioBySourceUser(currentUser);
            logger.info("Successfully retrieved portfolio data for {} sources for user: {}", data.size(), currentUser.getEmail());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("Error retrieving portfolio by source for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<PortfolioEntry>> getEntriesByCurrency(
            @PathVariable String currency,
            @RequestParam(required = false) Long accountId, @AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        logger.info("Received request to get entries by currency: {} for user: {}", currency, currentUser.getEmail());
        try {
            List<PortfolioEntry> entries;
            if (accountId != null) {
                entries = portfolioService.getEntriesByCurrencyAndAccountAndUser(currency, accountId, currentUser);
            } else {
                entries = portfolioService.getEntriesByCurrencyAndUser(currency, currentUser);
            }
            logger.info("Successfully retrieved {} entries for user: {}", entries.size(), currentUser.getEmail());
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Error retrieving entries by currency for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<List<PortfolioEntry>> getEntriesByCountry(
            @PathVariable String country,
            @RequestParam(required = false) Long accountId, @AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        logger.info("Received request to get entries by country: {} for user: {}", country, currentUser.getEmail());
        try {
            List<PortfolioEntry> entries;
            if (accountId != null) {
                entries = portfolioService.getEntriesByCountryAndAccountAndUser(country, accountId, currentUser);
            } else {
                entries = portfolioService.getEntriesByCountryAndUser(country, currentUser);
            }
            logger.info("Successfully retrieved {} entries for user: {}", entries.size(), currentUser.getEmail());
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Error retrieving entries by country for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/source/{source}")
    public ResponseEntity<List<PortfolioEntry>> getEntriesBySource(
            @PathVariable String source,
            @RequestParam(required = false) Long accountId, @AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        logger.info("Received request to get entries by source: {} for user: {}", source, currentUser.getEmail());
        try {
            List<PortfolioEntry> entries;
            if (accountId != null) {
                entries = portfolioService.getEntriesBySourceAndAccountAndUser(source, accountId, currentUser);
            } else {
                entries = portfolioService.getEntriesBySourceAndUser(source, currentUser);
            }
            logger.info("Successfully retrieved {} entries for user: {}", entries.size(), currentUser.getEmail());
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Error retrieving entries by source for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<PortfolioEntry>> getEntriesByType(
            @PathVariable EntryType type,
            @RequestParam(required = false) Long accountId, @AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        logger.info("Received request to get entries by type: {} for user: {}", type, currentUser.getEmail());
        try {
            List<PortfolioEntry> entries;
            if (accountId != null) {
                entries = portfolioService.getEntriesByTypeAndAccountAndUser(type, accountId, currentUser);
            } else {
                entries = portfolioService.getEntriesByTypeAndUser(type, currentUser);
            }
            logger.info("Successfully retrieved {} entries for user: {}", entries.size(), currentUser.getEmail());
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Error retrieving entries by type for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/combined/by-type")
    public ResponseEntity<Map<String, BigDecimal>> getCombinedPortfolioByType(@AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        logger.info("Received request to get portfolio grouped by type for user: {}", currentUser.getEmail());
        try {
            Map<String, BigDecimal> data = portfolioService.getCombinedPortfolioByTypeUser(currentUser);
            logger.info("Successfully retrieved portfolio data for {} types for user: {}", data.size(), currentUser.getEmail());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("Error retrieving portfolio by type for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/export/xlsx")
    public ResponseEntity<byte[]> exportToXlsx(@RequestParam(required = false) Long accountId, @AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        logger.info("Received request to export entries to XLSX for user: {}" + (accountId != null ? " for account " + accountId : ""), currentUser.getEmail());
        try {
            List<PortfolioEntry> entries;
            if (accountId != null) {
                entries = portfolioService.getEntriesByAccountAndUser(accountId, currentUser);
            } else {
                entries = portfolioService.getAllEntriesByUser(currentUser);
            }
            ByteArrayInputStream bis = exportService.exportEntriesToXlsx(entries);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=portfolio_entries.xlsx");
            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(bis.readAllBytes());
        } catch (IOException e) {
            logger.error("Error exporting entries to XLSX for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportToCsv(@RequestParam(required = false) Long accountId, @AuthenticationPrincipal OAuth2User principal) {
        User currentUser = getCurrentUser(principal);
        logger.info("Received request to export entries to CSV for user: {}" + (accountId != null ? " for account " + accountId : ""), currentUser.getEmail());
        try {
            List<PortfolioEntry> entries;
            if (accountId != null) {
                entries = portfolioService.getEntriesByAccountAndUser(accountId, currentUser);
            } else {
                entries = portfolioService.getAllEntriesByUser(currentUser);
            }
            ByteArrayInputStream bis = exportService.exportEntriesToCsv(entries);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=portfolio_entries.csv");
            return ResponseEntity.ok().headers(headers).contentType(MediaType.TEXT_PLAIN).body(bis.readAllBytes());
        } catch (IOException e) {
            logger.error("Error exporting entries to CSV for user {}: {}", currentUser.getEmail(), e.getMessage(), e);
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
