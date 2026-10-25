# Spec Delta

## Purpose

Defines how the Amusement Park application is built: a Maven project for JDK 21 derived from the quickstart archetype, with a test structure and an executable JAR.

## ADDED Requirements

### Requirement: Maven project based on the quickstart archetype
The project SHALL be a Maven project generated from `org.apache.maven.archetypes:maven-archetype-quickstart`, keeping its standard layout: `pom.xml` at the root, production code under `src/main/java` and test code under `src/test/java`.

#### Scenario: Standard layout present
- **WHEN** the repository is inspected
- **THEN** `pom.xml`, `src/main/java` and `src/test/java` exist following the quickstart archetype structure

### Requirement: Java 21 compilation
The project SHALL compile with JDK 21, targeting Java release 21.

#### Scenario: Compilation with JDK 21
- **WHEN** `mvn test` is run with JDK 21
- **THEN** the build compiles the sources for Java 21 and finishes successfully

### Requirement: Test structure
The project SHALL include a unit-test structure that runs as part of `mvn test`.

#### Scenario: Tests run during the build
- **WHEN** `mvn test` is executed
- **THEN** the unit tests under `src/test/java` are executed and pass

### Requirement: Executable JAR
Packaging the project SHALL produce a JAR whose manifest declares the application's main class, so it can be run with `java -jar`.

#### Scenario: Run packaged application
- **WHEN** `mvn package` is executed and the resulting JAR is run with `java -jar`
- **THEN** the application's main class starts without specifying a class name on the command line

### Requirement: Standard library only at runtime
The application SHALL depend only on the Java standard library at runtime; test-scoped dependencies are allowed.

#### Scenario: No runtime dependencies
- **WHEN** the `pom.xml` dependencies are inspected
- **THEN** every declared dependency has `test` scope
