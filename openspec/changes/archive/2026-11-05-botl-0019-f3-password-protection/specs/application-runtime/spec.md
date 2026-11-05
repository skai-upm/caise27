# Spec Delta

## MODIFIED Requirements

### Requirement: Delivery 1 technical restrictions
The application SHALL target Java 21. It SHALL use only the Java standard library at runtime (no databases, frameworks or external runtime libraries), with one exception from Delivery 3: the BCrypt library `org.mindrot:jbcrypt`, which is the only permitted runtime dependency and encryption mechanism and is used solely for password protection. Its production code SHALL NOT use regular expressions or any other encryption mechanism. Command parsing SHALL rely on basic String operations. The executable JAR SHALL bundle the BCrypt library so it runs without extra configuration.

#### Scenario: No runtime dependencies
- **GIVEN** the project `pom.xml`
- **WHEN** the declared dependencies are inspected
- **THEN** every dependency has `test` scope, except `org.mindrot:jbcrypt`

#### Scenario: No regular expressions or encryption in production code
- **GIVEN** the production sources under `src/main/java`
- **WHEN** they are inspected
- **THEN** they do not import `java.util.regex`, `javax.crypto` or `java.security`, nor call `matches`, `replaceAll`, `replaceFirst` or `Pattern`, and only the password hasher imports `org.mindrot.jbcrypt`

#### Scenario: Library bundled in the JAR
- **WHEN** `mvn package` produces `target/amusement-park.jar`
- **THEN** the JAR contains the BCrypt classes and starts with `java -jar`
