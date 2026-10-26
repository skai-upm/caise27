# Spec Delta

## ADDED Requirements

### Requirement: Password hashing dependency
The build SHALL declare the BCrypt library as a Maven dependency in `pom.xml`, and the executable JAR produced by `mvn package` SHALL include it so that `java -jar` works without additional classpath configuration.

#### Scenario: Dependency declared
- **WHEN** `pom.xml` is inspected
- **THEN** it declares the `org.mindrot:jbcrypt` dependency

#### Scenario: Self-contained JAR
- **WHEN** `mvn package` is run and `java -jar target/parque.jar` executes `USER CREATE` for a valid user
- **THEN** the user is created without class-loading errors

## REMOVED Requirements

### Requirement: No domain functionality
**Reason**: It described only the initial baseline; every later epic adds domain functionality, so it contradicts the current system.
**Migration**: None; the domain capabilities are specified in their own specs.
