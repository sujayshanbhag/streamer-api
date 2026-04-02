# Use an OpenJDK 21 base image
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/*.jar app.jar


# Expose the port the API runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]