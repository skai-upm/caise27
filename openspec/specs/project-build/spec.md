# project-build Specification

## Purpose

Defines how the amusement-park application is built, tested, and packaged so that every feature is delivered as a Maven project on Java 21 with an executable JAR.

## Requirements

### Requirement: Maven quickstart project structure
The project SHALL be a Maven project generated from `org.apache.maven.archetypes:maven-archetype-quickstart` and SHALL retain the archetype's standard directory layout, with production sources under `src/main/java` and test sources under `src/test/java`.

#### Scenario: Standard layout present
- **WHEN** the repository is inspected
- **THEN** it contains a `pom.xml` at the root, a main class under `src/main/java`, and a test class under `src/test/java`

### Requirement: Java 21 compilation
The build SHALL compile production and test sources for the Java 21 language level.

#### Scenario: Compiler release is 21
- **WHEN** the project is compiled with `mvn compile`
- **THEN** compilation succeeds and the produced class files target Java 21

### Requirement: Tests run with mvn test
The project SHALL include a basic unit-test structure and `mvn test` SHALL compile the project and run its tests successfully.

#### Scenario: Test phase succeeds
- **WHEN** `mvn test` is executed on a clean checkout
- **THEN** the build finishes successfully with all tests passing

### Requirement: Executable JAR
Packaging the project SHALL produce a JAR whose manifest declares the application's main class, so the JAR can be launched with `java -jar`.

#### Scenario: Run packaged JAR
- **WHEN** `mvn package` is executed and the resulting JAR is run with `java -jar`
- **THEN** the application's main class starts and exits normally without requiring a classpath argument

### Requirement: No domain functionality yet
The initial project SHALL NOT contain application-domain functionality; the main class only provides a minimal entry point.

#### Scenario: Minimal entry point
- **WHEN** the packaged application is run
- **THEN** it performs no domain operations and terminates normally
