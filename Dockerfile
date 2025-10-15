# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM gradle:7.6-jdk8 AS build
WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle ./
COPY src ./src

# Build the application
RUN gradle bootJar --no-daemon

# Stage 2: Runtime
FROM openjdk:8-jre-slim
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
