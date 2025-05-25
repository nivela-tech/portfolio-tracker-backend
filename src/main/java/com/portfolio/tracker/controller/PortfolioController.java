package com.portfolio.tracker.controller;

import com.portfolio.tracker.model.PortfolioEntry;
import com.portfolio.tracker.model.EntryType;
import com.portfolio.tracker.service.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;

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

    @PostMapping
    public ResponseEntity<PortfolioEntry> addEntry(@RequestBody PortfolioEntry entry) {
        logger.info("Received request to add new portfolio entry");
        try {
            PortfolioEntry savedEntry = portfolioService.addEntry(entry);
            logger.info("Successfully added entry with ID: {}", savedEntry.getId());
            return ResponseEntity.ok(savedEntry);
        } catch (Exception e) {
            logger.error("Error adding portfolio entry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortfolioEntry> updateEntry(@PathVariable Long id, @RequestBody PortfolioEntry entry) {
        logger.info("Received request to update portfolio entry with ID: {}", id);
        try {
            entry.setId(id);
            PortfolioEntry updatedEntry = portfolioService.updateEntry(entry);
            logger.info("Successfully updated entry with ID: {}", updatedEntry.getId());
            return ResponseEntity.ok(updatedEntry);
        } catch (EntityNotFoundException e) {
            logger.warn("Entry not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating portfolio entry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        logger.info("Received request to delete portfolio entry with ID: {}", id);
        try {
            portfolioService.deleteEntry(id);
            logger.info("Successfully deleted entry with ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            logger.warn("Entry not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting portfolio entry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<PortfolioEntry>> getAllEntries(@RequestParam(required = false) Long accountId) {
        logger.info("Received request to get all entries" + (accountId != null ? " for account " + accountId : ""));
        try {
            List<PortfolioEntry> entries;
            if (accountId != null) {
                entries = portfolioService.getEntriesByAccount(accountId);
            } else {
                entries = portfolioService.getAllEntries();
            }
            logger.info("Successfully retrieved {} entries", entries.size());
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Error retrieving portfolio entries: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/combined")
    public ResponseEntity<List<PortfolioEntry>> getCombinedPortfolio() {
        logger.info("Received request to get combined portfolio");
        try {
            List<PortfolioEntry> entries = portfolioService.getCombinedPortfolioEntries();
            logger.info("Successfully retrieved combined portfolio with {} entries", entries.size());
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Error retrieving combined portfolio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/combined/by-currency")
    public ResponseEntity<Map<String, BigDecimal>> getCombinedPortfolioByCurrency() {
        logger.info("Received request to get portfolio grouped by currency");
        try {
            Map<String, BigDecimal> data = portfolioService.getCombinedPortfolioByCurrency();
            logger.info("Successfully retrieved portfolio data for {} currencies", data.size());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("Error retrieving portfolio by currency: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/combined/by-country")
    public ResponseEntity<Map<String, BigDecimal>> getCombinedPortfolioByCountry() {
        logger.info("Received request to get portfolio grouped by country");
        try {
            Map<String, BigDecimal> data = portfolioService.getCombinedPortfolioByCountry();
            logger.info("Successfully retrieved portfolio data for {} countries", data.size());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("Error retrieving portfolio by country: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/combined/by-source")
    public ResponseEntity<Map<String, BigDecimal>> getCombinedPortfolioBySource() {
        logger.info("Received request to get portfolio grouped by source");
        try {
            Map<String, BigDecimal> data = portfolioService.getCombinedPortfolioBySource();
            logger.info("Successfully retrieved portfolio data for {} sources", data.size());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("Error retrieving portfolio by source: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<PortfolioEntry>> getEntriesByCurrency(
            @PathVariable String currency,
            @RequestParam(required = false) Long accountId) {
        logger.info("Received request to get entries by currency: {}", currency);
        try {
            List<PortfolioEntry> entries;
            if (accountId != null) {
                entries = portfolioService.getEntriesByCurrencyAndAccount(currency, accountId);
            } else {
                entries = portfolioService.getEntriesByCurrency(currency);
            }
            logger.info("Successfully retrieved {} entries", entries.size());
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Error retrieving entries by currency: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<List<PortfolioEntry>> getEntriesByCountry(
            @PathVariable String country,
            @RequestParam(required = false) Long accountId) {
        logger.info("Received request to get entries by country: {}", country);
        try {
            List<PortfolioEntry> entries;
            if (accountId != null) {
                entries = portfolioService.getEntriesByCountryAndAccount(country, accountId);
            } else {
                entries = portfolioService.getEntriesByCountry(country);
            }
            logger.info("Successfully retrieved {} entries", entries.size());
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Error retrieving entries by country: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/source/{source}")
    public ResponseEntity<List<PortfolioEntry>> getEntriesBySource(
            @PathVariable String source,
            @RequestParam(required = false) Long accountId) {
        logger.info("Received request to get entries by source: {}", source);
        try {
            List<PortfolioEntry> entries;
            if (accountId != null) {
                entries = portfolioService.getEntriesBySourceAndAccount(source, accountId);
            } else {
                entries = portfolioService.getEntriesBySource(source);
            }
            logger.info("Successfully retrieved {} entries", entries.size());
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Error retrieving entries by source: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<PortfolioEntry>> getEntriesByType(
            @PathVariable EntryType type,
            @RequestParam(required = false) Long accountId) {
        logger.info("Received request to get entries by type: {}", type);
        try {
            List<PortfolioEntry> entries;
            if (accountId != null) {
                entries = portfolioService.getEntriesByTypeAndAccount(type, accountId);
            } else {
                entries = portfolioService.getEntriesByType(type);
            }
            logger.info("Successfully retrieved {} entries", entries.size());
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Error retrieving entries by type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/combined/by-type")
    public ResponseEntity<Map<String, BigDecimal>> getCombinedPortfolioByType() {
        logger.info("Received request to get portfolio grouped by type");
        try {
            Map<String, BigDecimal> data = portfolioService.getCombinedPortfolioByType();
            logger.info("Successfully retrieved portfolio data for {} types", data.size());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("Error retrieving portfolio by type: {}", e.getMessage(), e);
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
