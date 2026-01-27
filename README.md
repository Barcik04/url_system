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
    
    ## Location 
















