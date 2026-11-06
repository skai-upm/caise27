# project-initialization Specification

## Purpose
Defines the build baseline of the Amusement Park application: a standard Maven project, created from the quickstart archetype, that targets Java 21 and builds from a clean checkout.

## Requirements

### Requirement: Standard Maven project structure
The repository SHALL contain a Maven project at its root, created from the Maven quickstart archetype, with a `pom.xml`, production sources under `src/main/java` and test sources under `src/test/java`.

#### Scenario: Project layout present in a clean checkout
- **GIVEN** a clean checkout of the repository
- **WHEN** the repository root is inspected
- **THEN** a `pom.xml` file exists at the root
- **AND** the directories `src/main/java` and `src/test/java` exist and contain Java sources

#### Scenario: Non-standard layout is not accepted
- **GIVEN** the project sources
- **WHEN** production code is placed outside `src/main/java`
- **THEN** the project does not satisfy this requirement

### Requirement: Java 21 compilation target
The project SHALL be configured so that Maven compiles production and test sources for Java release 21.

#### Scenario: Compiler release is 21
- **GIVEN** the `pom.xml`
- **WHEN** the effective compiler configuration is inspected
- **THEN** the Java release used for compilation is 21

#### Scenario: Language features above Java 21 are rejected
- **GIVEN** a source file that uses an API introduced after Java 21
- **WHEN** the project is compiled
- **THEN** compilation fails rather than producing classes for a newer release

### Requirement: Project compiles with Maven
The project SHALL build successfully with Maven from a clean checkout using a JDK that supports release 21, including compilation and execution of the test phase.

#### Scenario: Successful build from clean checkout
- **GIVEN** a clean checkout and a JDK supporting release 21 with Maven available
- **WHEN** `mvn -q test` is run at the repository root
- **THEN** the build succeeds and all tests pass

#### Scenario: Build output is not versioned
- **GIVEN** a successful build
- **WHEN** the working tree status is inspected
- **THEN** the generated `target/` directory is ignored and not committed
