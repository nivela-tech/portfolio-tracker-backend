package com.portfolio.tracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Configuration class to handle Railway DATABASE_URL format
 * Railway provides DATABASE_URL in format: postgresql://username:password@host:port/database
 * Spring expects: jdbc:postgresql://host:port/database
 */
@Configuration
@Profile("prod")
public class DataSourceConfig {

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
    public DataSource dataSource() {        // If it's already a JDBC URL, use it directly
        if (databaseUrl.startsWith("jdbc:")) {
            return DataSourceBuilder.create()
                    .url(databaseUrl)
                    .username(username)
                    .password(password)
                    .build();
        }
        
        // Otherwise, parse the Railway format (postgresql://)
        try {
            URI dbUri = new URI(databaseUrl);
            
            String dbUsername = username;
            String dbPassword = password;
            
            // If credentials are in the URL, extract them
            if (dbUri.getUserInfo() != null) {
                String[] userInfo = dbUri.getUserInfo().split(":");
                dbUsername = userInfo[0];
                if (userInfo.length > 1) {
                    dbPassword = userInfo[1];
                }
            }
            
            String dbHost = dbUri.getHost();
            int dbPort = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
            String dbPath = dbUri.getPath();
            
            // Form the proper JDBC URL
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s", dbHost, dbPort, dbPath);
            
            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(dbUsername)
                    .password(dbPassword)
                    .build();
            
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid DATABASE_URL", e);
        }
    }
}
