# Spec Delta

## MODIFIED Requirements

### Requirement: Standard library only
The application SHALL use only the Java standard library at runtime, except for the BCrypt library used to protect passwords; it MUST NOT use databases, frameworks, other external libraries, regular expressions or encryption mechanisms other than BCrypt password hashing. The executable JAR MUST include the BCrypt library so it runs without extra configuration.

#### Scenario: No runtime dependencies
- **WHEN** the project's dependencies are inspected
- **THEN** no dependency other than `org.mindrot:jbcrypt` and test-scoped testing libraries is declared

#### Scenario: Self-contained JAR
- **WHEN** `java -jar target/amusement-park.jar` creates a user
- **THEN** the password is hashed without a ClassNotFoundException
