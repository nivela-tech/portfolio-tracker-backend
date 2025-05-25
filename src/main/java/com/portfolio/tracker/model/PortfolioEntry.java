package com.portfolio.tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "portfolio_entries")
public class PortfolioEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Changed to LAZY for User
    @JoinColumn(name = "user_id", nullable = true) // Initially nullable for existing data
    @JsonIgnore // To prevent serialization issues, manage via DTOs if user info needed here
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private PortfolioAccount account;

    @Transient
    private Long accountId;

    public void setAccount(PortfolioAccount account) {
        this.account = account;
        if (account != null) {
            this.accountId = account.getId();
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
