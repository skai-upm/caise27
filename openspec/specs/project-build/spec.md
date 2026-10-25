# project-build Specification

## Purpose

Defines how the Amusement Park application is structured, built, tested and packaged as an executable Java 21 Maven project.

## Requirements

### Requirement: Maven quickstart project structure
The project SHALL be a Maven project generated from `org.apache.maven.archetypes:maven-archetype-quickstart`, keeping the archetype's standard layout (`src/main/java`, `src/test/java`) and its basic test structure.

#### Scenario: Standard layout is present
- **WHEN** the repository root is inspected
- **THEN** it contains `pom.xml`, main sources under `src/main/java` and tests under `src/test/java`

### Requirement: Tests run with mvn test
The project SHALL compile and pass all its tests when running `mvn test`.

#### Scenario: Test suite passes
- **WHEN** `mvn test` is executed on a clean checkout
- **THEN** the build finishes successfully with no test failures

### Requirement: Executable JAR
The build SHALL produce with `mvn package` an executable JAR whose manifest names the application's main class and which bundles its runtime dependencies, so it can be run with `java -jar` without extra classpath configuration.

#### Scenario: JAR runs with java -jar
- **WHEN** `mvn package` is executed and then `java -jar target/parque.jar` is run with `EXIT` on standard input
- **THEN** the application starts and terminates normally

#### Scenario: Dependencies bundled
- **WHEN** `java -jar target/parque.jar` creates a user (which hashes the password)
- **THEN** the command succeeds without adding jBCrypt to the classpath

### Requirement: Java 21 build with declared dependencies
The project SHALL compile for Java 21. Besides the Java standard library, its only runtime dependency SHALL be the jBCrypt library declared in `pom.xml`; all other dependencies SHALL be test-scoped.

#### Scenario: Build targets Java 21
- **WHEN** `mvn test` is executed
- **THEN** sources are compiled with release level 21 and the build succeeds

#### Scenario: Declared runtime dependency
- **WHEN** the project dependencies are listed
- **THEN** `org.mindrot:jbcrypt` is the only compile/runtime dependency
