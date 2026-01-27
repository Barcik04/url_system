

# Build stage: compile the Spring Boot application
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Maven wrapper and metadata
COPY mvnw .
COPY pom.xml .
COPY .mvn .mvn

RUN chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

# Copy the rest of the project
COPY . .
RUN chmod +x mvnw
RUN ./mvnw -B package -DskipTests


# Runtime stage: run the built JAR
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]