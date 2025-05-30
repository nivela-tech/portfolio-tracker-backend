package com.portfolio.tracker.config;

import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
@Profile("prod")
public class HealthConfig {

    private static final Logger logger = LoggerFactory.getLogger(HealthConfig.class);

    @Bean
    public HealthIndicator databaseHealthIndicator(DataSource dataSource) {
        return () -> {
            try {
                logger.debug("Checking database health...");
                try (Connection connection = dataSource.getConnection()) {
                    boolean isValid = connection.isValid(5); // 5 second timeout
                    if (isValid) {
                        logger.debug("Database connection is healthy");
                        return Health.up()
                                .withDetail("database", "PostgreSQL")
                                .withDetail("status", "Connected")
                                .build();
                    } else {
                        logger.warn("Database connection validation failed");
                        return Health.down()
                                .withDetail("database", "PostgreSQL")
                                .withDetail("status", "Connection validation failed")
                                .build();
                    }
                }
            } catch (Exception e) {
                logger.error("Database health check failed", e);
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }
}
