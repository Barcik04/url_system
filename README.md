### ABOUT
    --System allows users to have their own data base of url from which 
        system also creates a short version of. That esures link wont be
            a string of 2048 chars. User can also set expiry date on each link.
                Users can also check stats for each link and see how many times
                    their link has been clicked etc.


### SYSTEM
    --Register/Login Jwt tokenized authentication
    --Managing users links
    --Creating clickable shorted links


### TECH STACK
    ## Core
    - Java 21
    - Spring Boot 4.0.2
    - Maven

    ## Data & Persistence
    - PostgreSQL 17
    - Sping Data JPA (Hibernate)
    - Fly (db migrations)

    ## Security
    - Spring Security
    - Bucket4j (rate Limiting)
    - Jwt security 
    - Redis
    
    ## Validation & API
    - Spring Validation
    - REST API
    - Springdoc OpenAPI (Swagger)
    
    ## Testing
    - Spring Boot Test
    - JUnit5
    - Mockito
    - Testcointainers

    ## Obesrvability
    - Spring Boot Actuator 
    - JaCoCo
    - Micrometer + Prometheus
    - Grafana

    ##Messaging
    -Kafka

    ## CI / CD / Quality
    - Qodana
    - Github Actions
    - Dependabot 
    - Trivy

### HOW TO RUN LOCALLY 
    ## Prerequisites
    - Docker
    - Java 21

    ## Steps
    1. Clone Repository
    2. Set enviroment variable SPRING_PROFILES_ACTIVE=dev or SPRING_PROFILES_ACTIVE=prod
        -DEV:
            ```bash
            docker compose -f .\docker-compose.dev.yml up -d --build
        -PROD:
            ```bash
            docker compose -f .\docker-compose.prod.yml up -d --build
    
## ACCESS TO
        1. API: http://localhost:8080
        2. Swagger: http://localhost:8080/swagger-ui.html
        3. Actuator: http://localhost:8080/actuator/health
        4. JaCoCo raports at: Repository -> Actions -> Tests -> Test -> Scroll down
        5. Grafana: http://localhost:3000/  login: (admin/admin)
        6. Prometheus: http://localhost:9090/
        7. Kafka UI: http://localhost:8085/


### REQUIRED .env NAMES (ONLY NEEDED FROM PROD)
    `POSTGRES_DB`:  PostgreSQL database name 
    `POSTGRES_USER`:  PostgreSQL username
    `POSTGRES_PASSWORD`:  PostgreSQL password
    `SPRING_DATASOURCE_URL`:  JDBC connection URL
    `SPRING_DATASOURCE_USERNAME`:  Database username used by Spring
    `SPRING_DATASOURCE_PASSWORD`:  Database password used by Spring 
    
    --See ../.env.example file and adjust values if needed


### DB MIGRATIONS (FLYWAY)
    - location : ../src/main/resources/db/migration
    - behavior: Migrations automatically executed after API start


### AUTHENTICATION
    After successfull register and login, a JWT Bearer Token is issued to the client
    


### TESTING 
    Project uses JUnit5, Mockito and Testcontainers to cover diffrent parts of the project.
    **Mockito** test mostly used to check controller responses correctness.
    **Integration** tests used for buissness logic at service level.
    **TestContainers** ensuring correct interaction between all API layers

    ## Location: src/test/java/com/example/sports_management_system

    ## How to run:
            ```bash
            .\mvwn clean verify

    ## CI / CD in .github 
    -- qodana_code_quality.yml: 
        On every push and pull Code quality analysis is performed using Qodana
    -- docker-image.yml:
        On every pull docker image is being built and checked by Trivy which then is pushed to DockerHub
    -- tests.yml:
        tests are run and JaCoCo report is being issued
    -- depandabot.yml:
        checks vialability of dependencies versions

### ARCHITECTURE
    The project is a REST API built using a onion architecture
    
    The codebase is organized by groups (e.g. services, dtos, controllers), where each contains:
        -REST Controllers
        -dtos
        -exceptions
        -jwt (Bearer Authentication)
        -repostiories (JPA data access)
        -security   
        -services (buisness logic)
        - utils (rate-limit, logger, scheduling, redis, configs, emailSender)



### WHATS IMPLEMENTED
    -- Rate limiting ip or userId based on Redis 
    -- Actuator + Prometheus + Grafana
    -- JaCoCo
    -- Depandabot CI
    -- Qodana quality and Qoadana Tests
    -- Docker Image build and Push to DockerHub via GitHub Action workflows
    -- JWT
    -- Global Exception Handler
    -- Idempotency Keys secured
    -- Flyway Migrations (Tables, Constraints, Indexes)
    -- profiles dev-prod-test separation
    -- Mockito, Integration, Testcontainer Tests
    -- Custom Logger implementation with ID
    -- Resilience4j (Bulkhead, Retry, CircuitBreaker)
    -- Redis caching
    -- Async thread pool config
    -- Scheduled email sending on expired urls with Kafka (Async, Outbox)
    -- Kafka (email messaging)
    -- Dynamic filtering 
    -- Grafana Alerts










## WHAT TO DO
