### ABOUT
    This project is a URL shortening platform built as a REST API.
    
    It allows users to:
        - register and verify accounts via email,
        - create short URLs with optional expiration dates,
        - track click statistics for each link,
        - manage their own URL collection.
    
    The system focuses on security, observability and asynchronous processing.


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
    - Jwt security (with refresh tokens 30 days)
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
    - Kafka

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



### SYSTEM ARCHITECTURE
    The application follows an Onion Architecture:
    
        - Controllers – REST API endpoints
        - Dtos - APIs dtos
        - Exceptions - global excpetion handler plus custon exceptions
        - Jwt – JWT, authentication filters, 
        - Models - entities of system tables
        - Repositories – data access layer
        - Services – business logic
        - Utils - configs and side jobs like (async, filtering, email sender, logger, ratelimit, redis, scheduling, hashing)

    Asynchronous operations (emails, notifications) are handled using:
        - Outbox Pattern
        - Kafka messaging


### ASYNCHRONOUS PROCESSING
    Email delivery and side tasks use outbox pattern:
        1. System writes an outbox event to db
        2. Scheduled dispatcher publishes event to Kafka
        3. Kafka consumers polls events asynchronusly 
        4. Failures are retried and backedoff


### SECURITY
        - JWT authentication with refresh tokens (30 days)
        - Email verification required before account activation
        - Redis-based rate limiting with temporary bans
        - Idempotency keys to prevent duplicate requests
        - Secure password hashing

### HOSTING AWS
    - prod profile is used for AWS hosting
    - How to run on AWS:
        1. build Dockerfile and ./adot Dockerfile (for prometheus)
        2. Push both to ECR registry
        3. Boot up RDS PostgreSQL
        4. Boot up Amazon Prometheus
        4. Boot up EliastiCache for Redis
        5. Build task with two containers containing these Dockerfiles
        6. Connect container 1 to RDS, ElastiCache, SQS and container 2 to Amazon Prometheus
        7. Boot up Amazon Grafana and connect it to Prometheus
        8. Boot up Amazon SQS for queuing 
        9. Run service with created task


### WHATS IMPLEMENTED
    -- Rate limiting ip or userId based on Redis with auto blocking when user is doing way too much requests
    -- Actuator + Prometheus + Grafana
    -- JaCoCo
    -- Depandabot CI
    -- Qodana quality and Qoadana Tests
    -- Docker Image build and Push to DockerHub via GitHub Action workflows
    -- JWT (with refresh tokens of 30 days)
    -- Global Exception Handler
    -- Idempotency Keys secured
    -- Flyway Migrations (Tables, Constraints, Indexes)
    -- profiles dev-prod-test separation
    -- Mockito, Integration, Testcontainer Tests
    -- Custom Logger implementation with ID
    -- Resilience4j (Bulkhead, Retry, CircuitBreaker)
    -- Redis caching
    -- Async thread pool config
    -- Scheduled email sending on expired urls with Kafka (Async, Outbox) for dev, Amazon SQS for prod
    -- Kafka (email messaging)
    -- Dynamic filtering 
    -- Grafana Alerts
    










## WHAT TO DO
    -- AWS
    -- CD auto deploy to ECR
    -- react
    -- Kubernetes
    first ill generate a db schema graph from my migration files. Then architecture graph. Then lifecycle graph

    ### LEAVE IT FOR NOW:
        -- Admin panel (user list, disable/ban user, role changes)
        -- security tesst (JWT + authorization)
        -- contract tests
        -- performance tests

    ### For Future/Diffrent projects
        --Microservices
        --Chat between users
        --Payment process
        -- AI assistant for user support
        -- GraphQL 
        -- NO SQL (maybe)




### TOMORROW
    - BUILD ECS, Sercurity Groups from scratch 
    - ENV wars in secret manager?