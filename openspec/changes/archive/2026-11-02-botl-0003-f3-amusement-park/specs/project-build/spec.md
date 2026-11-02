# Spec Delta

## MODIFIED Requirements

### Requirement: Executable JAR
The build (`mvn package`) SHALL produce the executable JAR `target/parque.jar`, which includes the runtime dependencies and whose manifest entry point starts the application, so that it can be launched with `java -jar` without an IDE.

#### Scenario: Launch packaged application
- **WHEN** the project is packaged and the resulting JAR is launched with `java -jar`
- **THEN** the application starts and enters its command-execution path

### Requirement: Standard library only at runtime
The application SHALL use only the Java standard library and a BCrypt password-hashing library at runtime; no frameworks, databases, or other third-party runtime libraries SHALL be added.

#### Scenario: No runtime dependencies
- **WHEN** the project's declared dependencies are inspected
- **THEN** the only non-test dependency is the BCrypt library
