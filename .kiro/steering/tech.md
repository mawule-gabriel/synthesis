# Technology Stack

## Build System
- Maven (Apache Maven)
- Maven Wrapper included (mvnw/mvnw.cmd)

## Core Technologies
- Java 21
- Spring Boot 3.5.10
- Spring Web (RESTful services)
- Spring Data JPA (data persistence)
- PostgreSQL (database)
- Lombok (boilerplate reduction)

## Common Commands

### Build & Run
```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Package as JAR
./mvnw package
```

### Windows Commands
```cmd
# Build the project
mvnw.cmd clean install

# Run the application
mvnw.cmd spring-boot:run

# Run tests
mvnw.cmd test
```

## Configuration
- Application configuration: `src/main/resources/application.yaml`
- Uses YAML format for Spring Boot configuration
