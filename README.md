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

## Building the application

To build the application, use:
```bash
./gradlew clean build
```

Or use the provided batch script:
```bash
build-with-tracking.bat
```
