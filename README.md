# Flamefolio - Backend

This is the backend for the Flamefolio application.

It is built with Java and Spring Boot.

## Setup Requirements

1. Java 21 (see `JAVA_SETUP.md` for installation details)
2. PostgreSQL (see `POSTGRES_SETUP.md` for database setup)

## Database Management

This project uses Liquibase for database migrations. See `LIQUIBASE_GUIDE.md` for details on how to work with database migrations.

## Local Development

Before running the application, ensure that:
1. The PostgreSQL database is set up according to the instructions in `POSTGRES_SETUP.md`
2. Java 21 is configured as described in `JAVA_SETUP.md`

To run the backend server, navigate to the `backend` directory and use the following command:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Alternatively, you can use:
- The provided batch script: `run-app.bat`
- The VS Code task "Start Backend"

## Production Deployment

This application is configured for deployment on Railway. See the following guides for detailed instructions:

1. `RAILWAY_DEPLOYMENT.md` - Overview of Railway deployment configuration
2. `DEPLOYMENT_CHECKLIST.md` - Step-by-step checklist for deploying to Railway
3. `GOOGLE_OAUTH_SETUP.md` - Instructions for configuring Google OAuth for production

To deploy to Railway, you can use the provided scripts:

```bash
# For Windows
./deploy-railway.ps1

# For Unix/Linux
./deploy-railway.sh
```

### Environment Variables

The following environment variables should be set in your Railway project:

- `FRONTEND_URL`: The URL of the frontend application (https://firefolio.up.railway.app)
- `GOOGLE_CLIENT_ID`: Your Google OAuth2 client ID
- `GOOGLE_CLIENT_SECRET`: Your Google OAuth2 client secret  
- `SPRING_PROFILES_ACTIVE`: Set to 'prod' for production environment

Railway will automatically provide the following database variables:
- `DATABASE_URL`: The PostgreSQL connection URL
- `PGDATABASE`: Database name
- `PGHOST`: Database host
- `PGPASSWORD`: Database password
- `PGPORT`: Database port
- `PGUSER`: Database username
