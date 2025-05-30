# Use official OpenJDK runtime as base image
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew gradlew.bat build.gradle settings.gradle gradle.properties ./
COPY gradle/ gradle/

# Copy source code
COPY src/ src/

# Make gradlew executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew clean build -x check -x test

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "build/libs/portfolio-tracker-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]
