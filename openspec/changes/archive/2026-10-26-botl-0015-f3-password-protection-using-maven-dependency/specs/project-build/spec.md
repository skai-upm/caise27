# Spec Delta

## MODIFIED Requirements

### Requirement: Executable JAR
Packaging the project SHALL produce a JAR whose manifest declares the application's main class and which contains the runtime dependencies, so the JAR can be launched with `java -jar`.

#### Scenario: Run packaged JAR
- **WHEN** `mvn package` is executed and the resulting JAR is run with `java -jar`
- **THEN** the application's main class starts and exits normally without requiring a classpath argument

## ADDED Requirements

### Requirement: BCrypt dependency
The `pom.xml` SHALL declare the BCrypt library used for password hashing as a compile-scope Maven dependency.

#### Scenario: Dependency declared
- **WHEN** `pom.xml` is inspected
- **THEN** it declares the `org.mindrot:jbcrypt` dependency

## REMOVED Requirements

### Requirement: No domain functionality yet
**Reason**: It described the initial skeleton only; domain functionality has existed since Delivery 1.
**Migration**: None; the requirement no longer applies.
