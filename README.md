# Flamefolio - Backend

This is the backend for the Flamefolio application.

It is built with Java and Spring Boot.

## Setup Requirements

1. Java 21 (see `JAVA_SETUP.md` for installation details)
2. PostgreSQL (see `POSTGRES_SETUP.md` for database setup)

## Database Management

This project uses manual database management (no Liquibase). See `DATABASE_MANAGEMENT.md` for details on the database setup and management approach.

## Running the application

Before running the application, ensure that:
1. The PostgreSQL database is set up according to the instructions in `POSTGRES_SETUP.md`
2. Java 21 is configured as described in `JAVA_SETUP.md`

To run the backend server, navigate to the `backend` directory and use the following command:

```bash
./gradlew bootRun
```

Alternatively, you can use:
- The provided batch script: `run-app.bat`
- The VS Code task "Start Backend"

## Railway Deployment

This application is configured for deployment on Railway with the following files:
- `nixpacks.toml` - Configures the build environment and Java 21 setup
- `railway.toml` - Railway-specific deployment configuration

### Environment Variables Required on Railway:

1. **Database Configuration:**
   - `DATABASE_URL` - PostgreSQL connection string (automatically provided by Railway PostgreSQL service)
   - `DB_USERNAME` - Database username (if not using Railway's auto-generated DATABASE_URL)
   - `DB_PASSWORD` - Database password (if not using Railway's auto-generated DATABASE_URL)

2. **Application Configuration:**
   - `PORT` - Application port (set to 8080, automatically configured)
   - `FRONTEND_URL` - Your frontend application URL for CORS configuration

3. **OAuth Configuration (if needed):**
   - Update Google OAuth redirect URI to match your Railway deployment URL

### Deployment Steps:

1. Push your code to GitHub
2. Connect your GitHub repository to Railway
3. Add a PostgreSQL database service to your Railway project
4. Set the required environment variables in Railway dashboard
5. Deploy the application

The application will be available at your Railway-generated URL with health checks enabled at `/actuator/health`.

## Building the application

To build the application, use:
```bash
./gradlew clean build
```

Or use the provided batch script:
```bash
build-with-tracking.bat
```
