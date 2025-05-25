package com.portfolio.tracker.repository;

import com.portfolio.tracker.model.PortfolioAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioAccountRepository extends JpaRepository<PortfolioAccount, Long> {
}
