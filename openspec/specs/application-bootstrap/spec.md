# application-bootstrap Specification

## Purpose

Defines the build, layout, entry point, packaging and test baseline of the Java application so every later feature starts from a reproducible, verifiable Maven project.

## Requirements

### Requirement: Maven build targeting Java 21
The project SHALL be a Maven project whose sources compile for the Java 21 platform release.

#### Scenario: Fresh test build succeeds
- **GIVEN** a fresh checkout with Maven and a JDK able to target Java 21
- **WHEN** `mvn test` is run from the project root
- **THEN** compilation succeeds and all tests pass

#### Scenario: Compilation targets Java 21
- **WHEN** the build configuration is inspected
- **THEN** the compiler release level is 21

### Requirement: Standard Quickstart layout
The project SHALL keep the standard layout produced by `org.apache.maven.archetypes:maven-archetype-quickstart`: a root `pom.xml`, production sources under `src/main/java`, and test sources under `src/test/java`.

#### Scenario: Layout review
- **WHEN** the repository directory structure is reviewed
- **THEN** `pom.xml`, `src/main/java` and `src/test/java` are present with the archetype's package structure

### Requirement: Executable entry point
The project SHALL contain a main class with a `public static void main(String[])` entry point.

#### Scenario: Entry point runs
- **WHEN** the main class is executed
- **THEN** it starts and terminates normally without errors

### Requirement: Executable JAR packaging
Packaging the project SHALL produce a JAR whose manifest declares the entry-point class, so it can be started with `java -jar`.

#### Scenario: Packaged JAR starts
- **GIVEN** a successful `mvn package`
- **WHEN** `java -jar` is run on the produced JAR
- **THEN** the application starts without a "no main manifest attribute" error and exits with status 0

### Requirement: Basic test infrastructure
The project SHALL include a unit-testing framework dependency and at least one test under `src/test/java` that runs during `mvn test`.

#### Scenario: Tests are executed
- **WHEN** `mvn test` is run
- **THEN** the test report shows at least one executed test and zero failures

### Requirement: No premature domain functionality
This change SHALL NOT add amusement-park business functionality (users, commands, tickets, groups, attractions or persistence).

#### Scenario: Production code review
- **WHEN** the production sources are reviewed after this change
- **THEN** they contain only the entry-point class and no domain types
