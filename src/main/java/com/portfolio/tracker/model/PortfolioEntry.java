package com.portfolio.tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID; // Added for UUID

@Data
@Entity
@Table(name = "portfolio_entries")
public class PortfolioEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Changed strategy for UUID
    @Column(columnDefinition = "UUID") // Added for UUID
    private UUID id; // Changed type to UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Nullable true removed
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private PortfolioAccount account;

    @Transient
    private UUID accountId; // Changed type to UUID

    public void setAccount(PortfolioAccount account) {
        this.account = account;
        if (account != null) {
            this.accountId = account.getId(); // getId() will now return UUID
        } else {
            this.accountId = null;
        }
    }
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EntryType type = EntryType.STOCK;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String country;

    @Column(nullable = true) // Assuming notes can be optional
    private String notes;

    private LocalDateTime dateAdded = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (dateAdded == null) {
            dateAdded = LocalDateTime.now();
        }
    }
}
