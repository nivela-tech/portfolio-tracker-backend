package com.portfolio.tracker.repository;

import com.portfolio.tracker.model.PortfolioEntry;
import com.portfolio.tracker.model.EntryType;
import com.portfolio.tracker.model.User;
import com.portfolio.tracker.model.PortfolioAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // Added for UUID

public interface PortfolioRepository extends JpaRepository<PortfolioEntry, UUID> { // Changed Long to UUID
    List<PortfolioEntry> findAllByOrderByDateAddedDesc();

    // Methods for user-specific data
    Optional<PortfolioEntry> findByIdAndUser(UUID id, User user); // Changed Long to UUID
    List<PortfolioEntry> findByUserOrderByDateAddedDesc(User user);
    List<PortfolioEntry> findByAccountAndUserOrderByDateAddedDesc(PortfolioAccount account, User user);
    List<PortfolioEntry> findByCurrencyAndUser(String currency, User user);
    List<PortfolioEntry> findByCurrencyAndAccountAndUser(String currency, PortfolioAccount account, User user);
    List<PortfolioEntry> findByCountryAndUser(String country, User user);
    List<PortfolioEntry> findByCountryAndAccountAndUser(String country, PortfolioAccount account, User user);
    List<PortfolioEntry> findBySourceAndUser(String source, User user);
    List<PortfolioEntry> findBySourceAndAccountAndUser(String source, PortfolioAccount account, User user); // Corrected: PortfolioAccount instead of Long accountId
    List<PortfolioEntry> findByTypeAndUser(EntryType type, User user);
    List<PortfolioEntry> findByTypeAndAccountAndUser(EntryType type, PortfolioAccount account, User user);

    // Added missing methods that were causing errors in PortfolioService
    List<PortfolioEntry> findByUser(User user);
    List<PortfolioEntry> findByAccountAndUser(PortfolioAccount account, User user);

    @Query("SELECT e FROM PortfolioEntry e JOIN FETCH e.account WHERE e.user = :user ORDER BY e.dateAdded DESC")
    List<PortfolioEntry> findAllWithAccountsByUserOrderByDateAddedDesc(User user);

    @Query("SELECT e FROM PortfolioEntry e JOIN FETCH e.account ORDER BY e.dateAdded DESC")
    List<PortfolioEntry> findAllWithAccountsOrderByDateAddedDesc();
    
    // Admin/Non-user specific methods (consider security implications)
    List<PortfolioEntry> findByAccount_IdOrderByDateAddedDesc(UUID accountId); // Changed Long to UUID
    List<PortfolioEntry> findByCurrency(String currency);
    List<PortfolioEntry> findByCurrencyAndAccount_Id(String currency, UUID accountId); // Changed Long to UUID
    List<PortfolioEntry> findByCountry(String country);
    List<PortfolioEntry> findByCountryAndAccount_Id(String country, UUID accountId); // Changed Long to UUID
    List<PortfolioEntry> findBySource(String source);
    List<PortfolioEntry> findBySourceAndAccount_Id(String source, UUID accountId); // Changed Long to UUID
    List<PortfolioEntry> findByType(EntryType type);
    List<PortfolioEntry> findByTypeAndAccount_Id(EntryType type, UUID accountId); // Changed Long to UUID
}
