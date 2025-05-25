package com.portfolio.tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "portfolio_accounts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "entries"})
public class PortfolioAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) // Initially nullable for existing data
    @JsonIgnore // To prevent serialization issues, manage via DTOs if user info needed here
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
