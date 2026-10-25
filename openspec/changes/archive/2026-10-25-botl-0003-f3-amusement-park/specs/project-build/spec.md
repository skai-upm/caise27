# Spec Delta

## ADDED Requirements

### Requirement: Java 21 build with declared dependencies
The project SHALL compile for Java 21. Besides the Java standard library, its only runtime dependency SHALL be the jBCrypt library declared in `pom.xml`; all other dependencies SHALL be test-scoped.

#### Scenario: Build targets Java 21
- **WHEN** `mvn test` is executed
- **THEN** sources are compiled with release level 21 and the build succeeds

#### Scenario: Declared runtime dependency
- **WHEN** the project dependencies are listed
- **THEN** `org.mindrot:jbcrypt` is the only compile/runtime dependency

## MODIFIED Requirements

### Requirement: Executable JAR
The build SHALL produce with `mvn package` an executable JAR whose manifest names the application's main class and which bundles its runtime dependencies, so it can be run with `java -jar` without extra classpath configuration.

#### Scenario: JAR runs with java -jar
- **WHEN** `mvn package` is executed and then `java -jar target/parque.jar` is run with `EXIT` on standard input
- **THEN** the application starts and terminates normally

#### Scenario: Dependencies bundled
- **WHEN** `java -jar target/parque.jar` creates a user (which hashes the password)
- **THEN** the command succeeds without adding jBCrypt to the classpath

## REMOVED Requirements

### Requirement: Java 21 compilation
**Reason**: Delivery 3 adds jBCrypt as a runtime dependency, which contradicts the "standard library only" rule.
**Migration**: Replaced by "Java 21 build with declared dependencies".
