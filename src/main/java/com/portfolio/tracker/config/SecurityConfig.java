package com.portfolio.tracker.config;

import com.portfolio.tracker.service.CustomOidcUserService;
import com.portfolio.tracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.http.HttpStatus;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.web.cors.allowed-origins}")
    private String frontendUrl;

    @Autowired
    private CustomOidcUserService customOAuth2UserService;

    @Autowired
    private UserService userService; // You'll create this service

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {        http            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/index.html", "/static/**", "/manifest.json", "/favicon.ico", "/logo*.png", "/robots.txt").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/code/google").permitAll() // Allow OAuth2 related paths
                .requestMatchers("/actuator/**").permitAll() // Allow health checks for Railway
                .requestMatchers("/health", "/health/", "/ping", "/ping/").permitAll() // Allow simple health endpoints
                .requestMatchers("/api/user/me", "/api/portfolio/**", "/api/accounts/**", "/api/auth/**").authenticated() // Secure your API endpoints
                .anyRequest().authenticated()
            ).oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOAuth2UserService) // For OpenID Connect (like Google)
                )                
                .defaultSuccessUrl(frontendUrl + "/portfolio", true) // Redirect to frontend after login
                // .failureUrl("/login?error=true") // Optional: handle login failure
            )            .logout(logout -> logout
                .logoutUrl("/logout") // Default Spring Security logout URL
                .logoutSuccessUrl(frontendUrl + "/?logout=true") // Redirect to frontend after logout
                .invalidateHttpSession(true) // Invalidate the HTTP session
                .deleteCookies("JSESSIONID", "XSRF-TOKEN", "remember-me", "SESSION") // Delete all auth cookies
                .clearAuthentication(true) // Clear authentication
                .permitAll() // Allow logout for all users
            ).sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1) // Limit to one session per user
                .maxSessionsPreventsLogin(false) // Allow new login to invalidate old session
                .sessionRegistry(sessionRegistry()) // For session tracking
            ).csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Disable CSRF for API endpoints during development to troubleshoot 403 issues
                // In production, you should properly handle CSRF tokens instead of disabling protection
                .ignoringRequestMatchers("/api/**")
            )
            // If you want to return 401 instead of redirecting to login page for API calls from frontend
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            );
        return http.build();
    }    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            String email = oauthUser.getAttribute("email");
            // You might want to create or update the user in your database here
            userService.processOAuthPostLogin(email, oauthUser.getAttribute("name"), oauthUser.getAttribute("sub"), oauthUser.getAttribute("picture"));
              // Redirect to frontend or send a token
            // For now, let's just send a success status.
            // In a real app, you'd likely redirect to the frontend with a session or JWT.
            response.sendRedirect(frontendUrl + "/portfolio"); // Redirect to your frontend app
        };
    }
}
