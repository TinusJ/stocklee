# Use the official Eclipse Temurin image for Java 21
FROM eclipse-temurin:21-jdk as builder

# Set the working directory inside the container
WORKDIR /app

# Copy Maven wrapper and project files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Run Maven build to create the application JAR
RUN ./mvnw package -DskipTests

# Use a minimal JRE image for the runtime
FROM eclipse-temurin:21-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]