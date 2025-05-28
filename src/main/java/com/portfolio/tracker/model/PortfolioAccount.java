package com.portfolio.tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // Added for UUID

@Data
@Entity
@Table(name = "portfolio_accounts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "entries"})
public class PortfolioAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Changed strategy for UUID
    @Column(columnDefinition = "UUID") // Added for UUID
    private UUID id; // Changed type to UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Nullable true removed, assuming user_id is mandatory with UUIDs
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String relationship;

    @JsonManagedReference
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("account")
    private List<PortfolioEntry> entries = new ArrayList<>();

    // Helper method to manage bidirectional relationship
    public void addEntry(PortfolioEntry entry) {
        entries.add(entry);
        entry.setAccount(this);
    }

    public void removeEntry(PortfolioEntry entry) {
        entries.remove(entry);
        entry.setAccount(null);
    }
}
