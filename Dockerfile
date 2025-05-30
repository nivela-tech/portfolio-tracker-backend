# Multi-stage build: Frontend + Backend

# Stage 1: Build React frontend
FROM node:18-alpine AS frontend-build

WORKDIR /frontend

# Install git to clone the frontend repository
RUN apk add --no-cache git

# Clone the frontend repository
RUN git clone https://github.com/nivela-tech/portfolio-tracker-frontend.git .

# Install dependencies and build
RUN npm ci --only=production
RUN npm run build

# Stage 2: Build Spring Boot backend
FROM openjdk:21-jdk-slim AS backend-build

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew gradlew.bat build.gradle settings.gradle gradle.properties ./
COPY gradle/ gradle/

# Copy source code
COPY src/ src/

# Copy built frontend to Spring Boot static resources
COPY --from=frontend-build /frontend/build/ src/main/resources/static/

# Make gradlew executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew clean build -x check -x test

# Stage 3: Runtime
FROM openjdk:21-jre-slim

WORKDIR /app

# Copy the built jar from build stage
COPY --from=backend-build /app/build/libs/portfolio-tracker-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
