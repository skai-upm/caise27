# Spec Delta

## MODIFIED Requirements

### Requirement: Executable JAR
Running `mvn package` SHALL produce `target/parque.jar`, whose manifest declares the application's main class and which bundles the runtime dependencies, so that it can be started with `java -jar` alone.

#### Scenario: JAR runs directly
- **WHEN** a developer runs `mvn package` and then `java -jar target/parque.jar`
- **THEN** the application starts using the configured main class without requiring a classpath argument, and password hashing works

### Requirement: Standard library only
The application code SHALL use only the Java standard library at runtime plus a single BCrypt library declared in `pom.xml` for password hashing; no frameworks, databases or other runtime dependencies SHALL be introduced.

#### Scenario: No runtime dependencies
- **WHEN** the `pom.xml` dependencies are inspected
- **THEN** apart from test-scoped dependencies, only the BCrypt library is declared
