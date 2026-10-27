# project-build Specification

## Purpose

Defines the build contract of the Amusement Park project: a Maven project created from the quickstart archetype, targeting JDK 21 and buildable with Maven.

## Requirements

### Requirement: Maven quickstart project structure
The project SHALL be a Maven project at the repository root following the `maven-archetype-quickstart` layout: a `pom.xml`, production sources under `src/main/java` and test sources under `src/test/java`.

#### Scenario: Standard layout present
- **WHEN** the repository root is inspected
- **THEN** `pom.xml`, `src/main/java` and `src/test/java` exist and the sources are organised in a Java package

### Requirement: JDK 21 target
The project SHALL be configured to compile with Java release 21.

#### Scenario: Compiler release is 21
- **WHEN** the effective `pom.xml` is inspected
- **THEN** the Java compiler release is configured as `21`

### Requirement: Maven build succeeds
The project SHALL compile, run its tests and package successfully with Maven.

#### Scenario: Package the project
- **WHEN** `mvn package` is executed on a machine with JDK 21 or newer
- **THEN** the build finishes with `BUILD SUCCESS` and all unit tests pass

### Requirement: Milestone commit
The `pom.xml` and the basic project structure SHALL be committed with the exact message `[F0-C01] Initialize Maven project with JDK 21`.

#### Scenario: Commit present in history
- **WHEN** the Git history of the working branch is inspected
- **THEN** a commit with message `[F0-C01] Initialize Maven project with JDK 21` exists and contains `pom.xml`
