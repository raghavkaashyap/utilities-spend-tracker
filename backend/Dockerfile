# ==========================================
# Stage 1: Build the application
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build

# Set the current working directory inside the image
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application (this creates the target/ folder and the .jar file)
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Run the application
# ==========================================
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built .jar file from the 'build' stage
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Make port 8080 available
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
