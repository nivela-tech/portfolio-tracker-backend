package com.portfolio.tracker.repository;

import com.portfolio.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID; // Added for UUID

@Repository
public interface UserRepository extends JpaRepository<User, UUID> { // Changed Long to UUID
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderId(String providerId);
}
