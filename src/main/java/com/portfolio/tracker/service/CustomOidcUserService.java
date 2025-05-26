package com.portfolio.tracker.service;

import com.portfolio.tracker.model.User;
import com.portfolio.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        // Extract user details from OidcUser
        String providerId = oidcUser.getSubject(); // Use getSubject() for 'sub' claim
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String imageUrl = oidcUser.getPicture();

        Optional<User> userOptional = userRepository.findByProviderId(providerId);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setEmail(email); // Update email if changed
            user.setName(name);   // Update name if changed
            user.setImageUrl(imageUrl); // Update image if changed
            user.setLastLogin(LocalDateTime.now());
        } else {
            // If user not found by providerId, try by email (in case they signed up differently before)
            Optional<User> userByEmailOptional = userRepository.findByEmail(email);
            if (userByEmailOptional.isPresent()) {
                user = userByEmailOptional.get();
                user.setProviderId(providerId); // Link account
                user.setName(name);
                user.setImageUrl(imageUrl);
                user.setLastLogin(LocalDateTime.now());
            } else {
                user = new User(email, name, providerId, imageUrl);
                user.setLastLogin(LocalDateTime.now());
            }
        }
        userRepository.save(user);
        return oidcUser;
    }
}
