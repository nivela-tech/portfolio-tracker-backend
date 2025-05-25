package com.portfolio.tracker.repository;

import com.portfolio.tracker.model.PortfolioAccount;
import com.portfolio.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioAccountRepository extends JpaRepository<PortfolioAccount, Long> {
    List<PortfolioAccount> findByUser(User user);
    Optional<PortfolioAccount> findByIdAndUser(Long id, User user);
}
