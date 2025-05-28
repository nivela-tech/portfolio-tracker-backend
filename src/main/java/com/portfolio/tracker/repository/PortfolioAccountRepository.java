package com.portfolio.tracker.repository;

import com.portfolio.tracker.model.PortfolioAccount;
import com.portfolio.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID; // Added for UUID

public interface PortfolioAccountRepository extends JpaRepository<PortfolioAccount, UUID> { // Changed Long to UUID
    List<PortfolioAccount> findByUser(User user);
    Optional<PortfolioAccount> findByIdAndUser(UUID id, User user); // Changed Long to UUID
}
