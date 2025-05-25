package com.portfolio.tracker.repository;

import com.portfolio.tracker.model.PortfolioEntry;
import com.portfolio.tracker.model.EntryType;
import com.portfolio.tracker.model.User;
import com.portfolio.tracker.model.PortfolioAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<PortfolioEntry, Long> {
    List<PortfolioEntry> findAllByOrderByDateAddedDesc();

    // Methods for user-specific data
    Optional<PortfolioEntry> findByIdAndUser(Long id, User user);
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

    @Query("SELECT e FROM PortfolioEntry e JOIN FETCH e.account WHERE e.user = :user ORDER BY e.dateAdded DESC")
    List<PortfolioEntry> findAllWithAccountsByUserOrderByDateAddedDesc(User user);

    @Query("SELECT e FROM PortfolioEntry e JOIN FETCH e.account ORDER BY e.dateAdded DESC")
    List<PortfolioEntry> findAllWithAccountsOrderByDateAddedDesc();
    
    // Admin/Non-user specific methods (consider security implications)
    List<PortfolioEntry> findByAccount_IdOrderByDateAddedDesc(Long accountId);
    List<PortfolioEntry> findByCurrency(String currency);
    List<PortfolioEntry> findByCurrencyAndAccount_Id(String currency, Long accountId);
    List<PortfolioEntry> findByCountry(String country);
    List<PortfolioEntry> findByCountryAndAccount_Id(String country, Long accountId);
    List<PortfolioEntry> findBySource(String source);
    List<PortfolioEntry> findBySourceAndAccount_Id(String source, Long accountId); // This is for admin/non-user context
    List<PortfolioEntry> findByType(EntryType type);
    List<PortfolioEntry> findByTypeAndAccount_Id(EntryType type, Long accountId);
}
