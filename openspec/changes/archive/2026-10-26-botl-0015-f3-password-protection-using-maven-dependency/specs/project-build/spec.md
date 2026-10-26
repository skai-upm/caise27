# Spec Delta

## ADDED Requirements

### Requirement: BCrypt dependency
`pom.xml` SHALL declare the BCrypt library used for password hashing as a Maven dependency.

#### Scenario: Dependency declared
- **WHEN** `pom.xml` is inspected
- **THEN** it declares `org.mindrot:jbcrypt`

## MODIFIED Requirements

### Requirement: Executable JAR
Packaging the project SHALL produce a JAR whose manifest declares the application's main class and that contains the runtime dependencies, so it can be run with `java -jar` without extra classpath configuration.

#### Scenario: Run the packaged JAR
- **WHEN** a developer runs `mvn package` and then `java -jar` on the produced JAR
- **THEN** the application's main class runs and terminates normally

#### Scenario: Dependencies available at run time
- **WHEN** `java -jar target/parque.jar` creates a user
- **THEN** the password is hashed without a `ClassNotFoundException`

## REMOVED Requirements

### Requirement: No domain functionality yet
**Reason**: Only applied to the initial project skeleton; the application now implements its domain.
**Migration**: None; domain behaviour is specified by the domain capabilities.
