## MODIFIED Requirements

### Requirement: Standard library only at runtime
The application SHALL use only the Java standard library and the BCrypt library `org.mindrot:jbcrypt:0.4` at runtime, bundled into the executable JAR: no databases, frameworks, other external libraries or regular expressions.

#### Scenario: No runtime dependencies
- **WHEN** the project's dependencies are inspected
- **THEN** the only dependency not in `test` scope is `org.mindrot:jbcrypt:0.4`

#### Scenario: Self-contained JAR
- **WHEN** `java -jar target/nombre-aplicacion.jar` is run with no extra classpath
- **THEN** password hashing works without missing-class errors
