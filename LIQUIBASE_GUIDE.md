# Liquibase Database Migration Guide

This project uses Liquibase for database schema versioning and migration with SQL files.

## Overview

Liquibase is an open-source database-independent library for tracking, managing, and applying database schema changes. It helps maintain database changes in a structured and organized way across different environments.

## Structure

- `src/main/resources/db/changelog/db.changelog-master.yaml`: The master changelog file that includes all other changelogs
- `src/main/resources/db/changelog/changes/`: Directory containing individual SQL changelog files

## Existing Changelogs

1. `001-initial-schema.sql`: Initial database schema with tables for users, accounts, and portfolio entries
2. `002-add-indexes.sql`: Adds performance indexes to the database tables

## How to Create a New Migration

1. Create a new SQL file in the `src/main/resources/db/changelog/changes/` directory
2. Name it with a sequential number and a descriptive name (e.g., `003-add-new-feature.sql`)
3. Include the new file in the master changelog (`db.changelog-master.yaml`)

## Example SQL Changelog Format

```sql
--liquibase formatted sql
--changeset your-name:003

-- Add your SQL statements here
ALTER TABLE existing_table ADD COLUMN new_column VARCHAR(255);

-- You can use comments to document your changes
CREATE TABLE new_table (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Adding to Master Changelog

To add your new SQL file to the master changelog, update the `db.changelog-master.yaml` file:

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/changes/001-initial-schema.sql
      
  - include:
      file: db/changelog/changes/002-add-indexes.sql
      
  - include:
      file: db/changelog/changes/003-add-new-feature.sql
```

## Common Liquibase Commands

Liquibase is automatically run by Spring Boot at startup, but you can also run it manually:

```bash
# Generate SQL from changelog (without executing)
./gradlew liquibaseDiffSQL

# Update database schema
./gradlew liquibaseUpdate

# Roll back the last change
./gradlew liquibaseRollbackCount -PliquibaseCommandValue=1
```

## Best Practices

1. Each change should have a unique ID and author
2. Changes should be atomic and focused on a single logical change
3. Never modify existing changelogs that have been applied to a database
4. Always create new changelogs for new changes
5. Test all migrations in a development environment before applying to production

## Railway Deployment

The database migration will automatically run when the application is deployed to Railway. The application is configured to use the Railway environment variables for database connection.

## Local Development

For local development, the application uses the configuration in `application-local.properties`. Make sure your local PostgreSQL instance is running and accessible.
