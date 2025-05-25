package com.portfolio.tracker.service;

import com.portfolio.tracker.model.User;
import com.portfolio.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User processOAuthPostLogin(String email, String name, String providerId, String imageUrl) {
        Optional<User> userOptional = userRepository.findByProviderId(providerId);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update user details if they have changed
            user.setEmail(email);
            user.setName(name);
            user.setImageUrl(imageUrl);
            user.setLastLogin(LocalDateTime.now());
        } else {
             // If user not found by providerId, try by email (in case they signed up differently before)
            Optional<User> userByEmailOptional = userRepository.findByEmail(email);
            if(userByEmailOptional.isPresent()){
                user = userByEmailOptional.get();
                user.setProviderId(providerId); // Link account
                user.setName(name);
                user.setImageUrl(imageUrl);
                user.setLastLogin(LocalDateTime.now());
            } else {
                user = new User();
                user.setEmail(email);
                user.setName(name);
                user.setProviderId(providerId);
                user.setImageUrl(imageUrl);
                user.setCreatedAt(LocalDateTime.now());
                user.setLastLogin(LocalDateTime.now());
            }
        }
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId);
    }
}
