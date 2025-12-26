# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21-jdk-jammy

# Set the working directory to /app
WORKDIR /app

# Install dependencies and Node.js
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Verify Node.js and npm installation
RUN node -v && npm -v

# Copy the packaged JAR file into the container at /app
COPY target/codelab-*.jar app.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080
EXPOSE 8000

# Run the JAR file
CMD ["java", "-jar", "app.jar"]