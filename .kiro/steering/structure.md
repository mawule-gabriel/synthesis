# Project Structure

## Package Organization
Base package: `com.asakaa.synthesis`

```
src/
├── main/
│   ├── java/com/asakaa/synthesis/
│   │   └── SynthesisApplication.java (main entry point)
│   └── resources/
│       ├── application.yaml (configuration)
│       ├── static/ (static web resources)
│       └── templates/ (view templates)
└── test/
    └── java/com/asakaa/synthesis/
        └── SynthesisApplicationTests.java
```

## Conventions

### Java Code
- Use Lombok annotations to reduce boilerplate (@Data, @Builder, etc.)
- Follow Spring Boot conventions for component organization
- Place controllers, services, repositories, and entities in respective packages under base package

### Testing
- Test classes mirror main source structure
- Use Spring Boot test annotations (@SpringBootTest, @WebMvcTest, etc.)
- Place test resources in `src/test/resources/`

### Configuration
- Use YAML format for application properties
- Environment-specific configs: `application-{profile}.yaml`
