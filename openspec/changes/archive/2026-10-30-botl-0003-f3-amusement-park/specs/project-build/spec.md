# Spec Delta

## MODIFIED Requirements

### Requirement: Standard library only at runtime
The application SHALL use only the Java standard library at runtime plus the BCrypt library `org.mindrot:jbcrypt:0.4` for password protection: no databases, frameworks, other external libraries or regular expressions. The executable JAR SHALL bundle the BCrypt library so it runs with `java -jar` alone.

#### Scenario: No runtime dependencies
- **WHEN** the project's dependencies are inspected
- **THEN** the only dependency with compile or runtime scope is `org.mindrot:jbcrypt:0.4` (other dependencies are test-scoped)

#### Scenario: No regular expressions in source
- **WHEN** the application source code is inspected
- **THEN** it does not use `java.util.regex`, `String.matches`, `String.replaceAll`, `String.replaceFirst`, or regex-based `split` patterns other than the literal `;`, `@` and `.` separators

#### Scenario: Self-contained JAR
- **WHEN** `java -jar target/nombre-aplicacion.jar` is run from a directory without the Maven repository
- **THEN** the application starts and can create a user (BCrypt classes are inside the JAR)
