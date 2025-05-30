# Multi-stage build for Spring Boot backend

# Stage 1: Build Spring Boot backend
FROM eclipse-temurin:21-jdk AS backend-build

# Set working directory
WORKDIR /app

# Set JAVA_HOME explicitly 
ENV JAVA_HOME=/opt/java/openjdk

# Copy gradle wrapper and build files
COPY gradlew gradlew.bat build.gradle settings.gradle gradle.properties ./
COPY gradle/ gradle/

# Copy source code (including pre-built frontend static resources)
COPY src/ src/

# Make gradlew executable
RUN chmod +x gradlew

# Configure Gradle memory settings
ENV GRADLE_OPTS="-Xmx512m -Xms128m"

# Print Java version and location for debugging
RUN which java
RUN java -version
RUN echo $JAVA_HOME

# Inspect the gradlew script to understand the issue
RUN cat gradlew | grep JAVA_HOME

# Fix the gradlew script to accept our JAVA_HOME
RUN sed -i 's|/usr/lib/jvm/java-21-openjdk|/opt/java/openjdk|g' gradlew

# Build the application with debug output
RUN ./gradlew clean build -x check -x test --info --stacktrace

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built jar from build stage
COPY --from=backend-build /app/build/libs/portfolio-tracker-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-Xmx512m", "-Xms256m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar", "--spring.profiles.active=prod"]
