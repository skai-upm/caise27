# Spec Delta

## MODIFIED Requirements

### Requirement: Executable JAR
Packaging the project SHALL produce a JAR whose manifest declares the application's main class and that contains the project's runtime dependencies, so the JAR can be launched with `java -jar` without any additional classpath.

#### Scenario: Run packaged JAR
- **WHEN** `mvn package` is run and then `java -jar target/parque.jar` is executed
- **THEN** the application starts through its main class and exits normally

#### Scenario: Dependencies available at runtime
- **WHEN** the packaged JAR creates a user (which hashes the password with the BCrypt library)
- **THEN** the command succeeds without a missing-class error

## ADDED Requirements

### Requirement: BCrypt dependency
`pom.xml` SHALL declare the BCrypt library used to hash passwords as a Maven dependency with compile scope.

#### Scenario: Dependency declared
- **WHEN** `pom.xml` is inspected
- **THEN** it declares the `org.mindrot:jbcrypt` dependency
