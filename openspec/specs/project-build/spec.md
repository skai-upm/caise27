# project-build Specification

## Purpose

Defines how the amusement park application is structured and built: a Maven project with the quickstart archetype layout, targeting JDK 21, that every later delivery extends.

## Requirements

### Requirement: Maven quickstart project structure
The project SHALL be a Maven project created with the Apache Maven quickstart archetype, with `pom.xml` at the repository root, production sources under `src/main/java` and test sources under `src/test/java`.

#### Scenario: Standard layout present
- **WHEN** the repository root is inspected
- **THEN** it contains `pom.xml`, a `src/main/java` source tree and a `src/test/java` test tree

#### Scenario: No default package
- **WHEN** the Java sources are inspected
- **THEN** every class is declared inside a named package

### Requirement: JDK 21 target
The Maven build SHALL be configured to compile the sources for Java release 21.

#### Scenario: Compiler release configured
- **WHEN** the effective `pom.xml` configuration is inspected
- **THEN** the Java compiler release level is 21

### Requirement: Compilable with Maven
The project SHALL compile and pass its tests using Maven without additional manual setup beyond a JDK 21 (or newer) and Maven.

#### Scenario: Package build succeeds
- **WHEN** `mvn package` is executed at the repository root
- **THEN** the build finishes successfully, compiling the sources and running the unit tests without failures
