# project-initialization Specification

## Purpose

Defines the build baseline of the Amusement Park application: a standard Maven quickstart project targeting JDK 21 that compiles with Maven.

## Requirements

### Requirement: Standard Maven project structure
The repository SHALL contain a Maven project created from the `maven-archetype-quickstart` archetype, with a `pom.xml` at the repository root, production sources under `src/main/java` and test sources under `src/test/java`.

#### Scenario: Clean checkout has the standard layout
- **GIVEN** a clean checkout of the repository
- **WHEN** the repository root is inspected
- **THEN** `pom.xml`, `src/main/java` and `src/test/java` are present

#### Scenario: Non-Maven structure is not accepted
- **GIVEN** a checkout without a root `pom.xml`
- **WHEN** the project is built with Maven
- **THEN** the build fails and the project does not satisfy this requirement

### Requirement: JDK 21 target
The project SHALL be configured in `pom.xml` to compile Java sources for Java release 21.

#### Scenario: Compiler release is 21
- **WHEN** the effective compiler configuration of `pom.xml` is inspected
- **THEN** the Java release used for compilation is `21`

### Requirement: Compilable with Maven
The project MUST compile successfully with Maven on a JDK supporting release 21.

#### Scenario: Successful compilation
- **GIVEN** a clean checkout and JDK 21 with Maven available
- **WHEN** `mvn compile` is executed
- **THEN** the build finishes with `BUILD SUCCESS` and produces compiled classes under `target/classes`

#### Scenario: Unsupported JDK fails explicitly
- **GIVEN** a JDK older than 21
- **WHEN** `mvn compile` is executed
- **THEN** the build fails with an error about the unsupported release, and no classes are produced
