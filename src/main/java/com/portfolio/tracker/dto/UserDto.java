package com.portfolio.tracker.dto;

public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String provider;

    public UserDto(Long id, String name, String email, String provider) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.provider = provider;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
