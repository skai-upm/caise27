# Spec Delta

## MODIFIED Requirements

### Requirement: Standard library only at runtime
The application SHALL use only the Java standard library at runtime, except for the BCrypt library `org.mindrot:jbcrypt` used for password protection, which SHALL be packaged inside the executable JAR. It SHALL NOT use databases, frameworks, other external libraries, regular expressions for command parsing or validation, or Java object serialization.

#### Scenario: No runtime dependencies
- **WHEN** the project's dependency tree is inspected
- **THEN** the only compile- or runtime-scope dependency is `org.mindrot:jbcrypt` (test-scope dependencies are permitted)

#### Scenario: Self-contained JAR
- **WHEN** `java -jar target/amusement-park.jar` is run without any classpath configuration
- **THEN** password hashing works because the BCrypt classes are inside the JAR
