package com.portfolio.tracker.repository;

import com.portfolio.tracker.model.PortfolioEntry;
import com.portfolio.tracker.model.EntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PortfolioRepository extends JpaRepository<PortfolioEntry, Long> {
    List<PortfolioEntry> findAllByOrderByDateAddedDesc();
    
    @Query("SELECT e FROM PortfolioEntry e JOIN FETCH e.account ORDER BY e.dateAdded DESC")
    List<PortfolioEntry> findAllWithAccountsOrderByDateAddedDesc();
    
    List<PortfolioEntry> findByAccount_IdOrderByDateAddedDesc(Long accountId);
    List<PortfolioEntry> findByCurrency(String currency);
    List<PortfolioEntry> findByCurrencyAndAccount_Id(String currency, Long accountId);
    List<PortfolioEntry> findByCountry(String country);
    List<PortfolioEntry> findByCountryAndAccount_Id(String country, Long accountId);
    List<PortfolioEntry> findBySource(String source);
    List<PortfolioEntry> findBySourceAndAccount_Id(String source, Long accountId);
    List<PortfolioEntry> findByType(EntryType type);
    List<PortfolioEntry> findByTypeAndAccount_Id(EntryType type, Long accountId);
}
