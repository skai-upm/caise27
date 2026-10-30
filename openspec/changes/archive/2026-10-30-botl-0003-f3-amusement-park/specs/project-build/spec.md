# Spec Delta

## MODIFIED Requirements

### Requirement: Standard library only at runtime
The application SHALL use only the Java standard library at runtime plus the jBCrypt library (`org.mindrot:jbcrypt:0.4`) for password protection, which the executable JAR SHALL bundle; it SHALL NOT use databases, frameworks, other external libraries or regular expressions.

#### Scenario: Packaged JAR has no third-party runtime dependencies
- **WHEN** the project's runtime dependencies are inspected
- **THEN** the only compile- or runtime-scoped dependency is `org.mindrot:jbcrypt`, and `java -jar target/amusement-park.jar` runs without extra classpath configuration
