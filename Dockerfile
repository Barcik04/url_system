FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# cache dependencies
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# copy sources and build
COPY src ./src
RUN mvn -B -DskipTests clean package


FROM eclipse-temurin:21-jre
WORKDIR /app

# run as non-root
RUN useradd -m appuser
USER appuser

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENV SERVER_PORT=8080

ENTRYPOINT ["java","-XX:+UseG1GC","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]
