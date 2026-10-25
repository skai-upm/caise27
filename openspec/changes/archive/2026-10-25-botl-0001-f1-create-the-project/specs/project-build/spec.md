# Spec Delta

## Purpose

Defines how the amusement-park application is built, tested and packaged: a Java 21 Maven project with the standard quickstart layout that produces an executable JAR.

## ADDED Requirements

### Requirement: Maven project generated from the quickstart archetype
The application SHALL be a Maven project generated from `org.apache.maven.archetypes:maven-archetype-quickstart`, retaining its standard directory structure (`pom.xml`, `src/main/java`, `src/test/java`).

#### Scenario: Standard layout present
- **WHEN** the repository is inspected
- **THEN** a `pom.xml` exists at the project root, production sources live under `src/main/java` and test sources under `src/test/java`

### Requirement: Compilation with JDK 21
The build SHALL compile the sources targeting Java 21.

#### Scenario: Build targets Java 21
- **WHEN** `mvn test` is run with a JDK of version 21 or newer
- **THEN** the sources are compiled for the Java 21 release and the build succeeds

### Requirement: Basic testing structure
The project SHALL include a test source tree with at least one unit test executed by `mvn test`.

#### Scenario: Tests run successfully
- **WHEN** `mvn test` is run
- **THEN** the test phase executes the project's unit tests and reports no failures

### Requirement: Executable JAR with a main class
The project SHALL include an executable main class, and `mvn package` SHALL produce a JAR whose manifest declares that class as `Main-Class`, so it can be launched with `java -jar`.

#### Scenario: Running the packaged JAR
- **WHEN** `mvn package` is run and the resulting JAR is launched with `java -jar`
- **THEN** the main class runs and the process exits normally

### Requirement: No domain functionality yet
This initial project SHALL NOT contain application-domain functionality; the main class only provides a minimal entry point.

#### Scenario: Minimal entry point
- **WHEN** the application is run
- **THEN** it only prints a minimal startup message and terminates without performing any domain operation
