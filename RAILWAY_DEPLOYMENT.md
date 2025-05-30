# Portfolio Tracker Backend - Railway Deployment Guide

This is the backend service for the Portfolio Tracker application, deployed on Railway.

## Deployment Configuration

The application is configured to run on Railway with PostgreSQL database integration using Liquibase for database migrations.

### Environment Variables

The following environment variables are used by the application in Railway:

Database connection (provided by Railway PostgreSQL plugin):
- `DATABASE_URL`: The PostgreSQL connection URL
- `PGDATABASE`: Database name
- `PGHOST`: Database host
- `PGPASSWORD`: Database password 
- `PGPORT`: Database port
- `PGUSER`: Database username

Application configuration:
- `FRONTEND_URL`: The URL of the frontend application for CORS configuration
- `PORT`: The port the application should run on (provided by Railway)

### Database Migration

The application uses Liquibase to manage database migrations. When the application starts, Liquibase will:

1. Check if the changelog tables exist, and create them if they don't
2. Apply any new changelogs that haven't been run yet

The migration files are located in:
- `src/main/resources/db/changelog/db.changelog-master.yaml`: Master changelog
- `src/main/resources/db/changelog/changes/*.sql`: Individual SQL changelog files

## Local Development

For local development:

1. Set up a PostgreSQL database
2. Configure the connection in `application-local.properties`
3. Run the application with the `local` profile: `./gradlew bootRun --args='--spring.profiles.active=local'`

## Building and Running

The application is built and deployed using Docker. The Dockerfile includes:

1. Building the application with Gradle
2. Running the application with the production profile

## Maintenance

To add new database changes, follow these steps:

1. Create a new SQL changelog file in `/src/main/resources/db/changelog/changes/`
2. Add it to the master changelog
3. Test locally
4. Push to GitHub to trigger deployment

See the [Liquibase Guide](LIQUIBASE_GUIDE.md) for more details on working with database migrations.
