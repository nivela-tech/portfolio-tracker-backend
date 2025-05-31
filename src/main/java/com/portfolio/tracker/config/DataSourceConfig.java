package com.portfolio.tracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Configuration class to handle Railway DATABASE_URL format
 * Railway provides DATABASE_URL in format: postgresql://username:password@host:port/database
 * Spring expects: jdbc:postgresql://host:port/database
 */
@Configuration
@Profile("prod")
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${DATABASE_URL:jdbc:postgresql://localhost:5432/portfolio_db}")
    private String databaseUrl;
    
    @Value("${PGUSER:portfolio_user}")
    private String username;
    
    @Value("${PGPASSWORD:portfolio@123}")
    private String password;

    /**
     * Creates a DataSource bean that handles the Railway DATABASE_URL format
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        logger.info("=== DATABASE CONFIGURATION ===");
        logger.info("Raw DATABASE_URL length: {}", databaseUrl != null ? databaseUrl.length() : "null");
        logger.info("DATABASE_URL starts with 'postgresql://': {}", databaseUrl != null && databaseUrl.startsWith("postgresql://"));
        logger.info("DATABASE_URL starts with 'jdbc:': {}", databaseUrl != null && databaseUrl.startsWith("jdbc:"));
        logger.info("PGUSER: {}", username != null ? username : "null");
        logger.info("PGPASSWORD present: {}", password != null && !password.isEmpty());
        
        DataSource dataSource;
        
        // If it's already a JDBC URL, use it directly
        if (databaseUrl.startsWith("jdbc:")) {
            logger.info("Using direct JDBC URL format");
            dataSource = DataSourceBuilder.create()
                    .url(databaseUrl)
                    .username(username)
                    .password(password)
                    .build();
        } else {
            logger.info("Parsing Railway DATABASE_URL format");
            dataSource = parseRailwayDatabaseUrl();
        }
        
        // Test the connection
        try {
            logger.info("Testing database connection...");
            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(10);
                logger.info("Database connection test result: {}", isValid ? "SUCCESS" : "FAILED");
                if (isValid) {
                    logger.info("Database URL: {}", connection.getMetaData().getURL());
                    logger.info("Database Product: {}", connection.getMetaData().getDatabaseProductName());
                    logger.info("Database Version: {}", connection.getMetaData().getDatabaseProductVersion());
                }
            }
        } catch (SQLException e) {
            logger.error("Database connection test FAILED", e);
        }
        
        logger.info("=== END DATABASE CONFIGURATION ===");
        return dataSource;
    }
    
    private DataSource parseRailwayDatabaseUrl() {
        try {
            logger.debug("Parsing DATABASE_URL: {}", databaseUrl.replaceAll(":[^:/@]+@", ":****@")); // Mask password in logs
            URI dbUri = new URI(databaseUrl);
            
            String dbUsername = username;
            String dbPassword = password;
            
            // If credentials are in the URL, extract them
            if (dbUri.getUserInfo() != null) {
                logger.debug("Extracting credentials from DATABASE_URL");
                String[] userInfo = dbUri.getUserInfo().split(":");
                dbUsername = userInfo[0];
                if (userInfo.length > 1) {
                    dbPassword = userInfo[1];
                }
                logger.debug("Extracted username: {}", dbUsername);
                logger.debug("Extracted password present: {}", dbPassword != null && !dbPassword.isEmpty());
            }
            
            String dbHost = dbUri.getHost();
            int dbPort = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
            String dbPath = dbUri.getPath();
            
            logger.info("Database host: {}", dbHost);
            logger.info("Database port: {}", dbPort);
            logger.info("Database path: {}", dbPath);
            
            // Form the proper JDBC URL
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s", dbHost, dbPort, dbPath);
            logger.info("Constructed JDBC URL: {}", jdbcUrl);
            
            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(dbUsername)
                    .password(dbPassword)
                    .build();
            
        } catch (URISyntaxException e) {
            logger.error("Invalid DATABASE_URL format", e);
            throw new RuntimeException("Invalid DATABASE_URL", e);
        }
    }
}
