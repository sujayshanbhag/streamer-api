# Use an OpenJDK 21 base image
FROM eclipse-temurin:21-jre-jammy

# Install ffmpeg and ffprobe
RUN apt-get update && apt-get install -y ffmpeg && rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY streamer-transcoder/target/streamer-transcoder.jar app.jar

# Expose the port the Transcoder service runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]