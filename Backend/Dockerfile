# Use Java 17
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy jar
COPY target/*.jar app.jar

# Expose microservice port
EXPOSE 8081

# Run app
ENTRYPOINT ["java","-jar","app.jar"]
