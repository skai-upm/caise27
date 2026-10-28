# Spec Delta

## Purpose

Defines how the Amusement Park project is structured and built: a Maven project created from the quickstart archetype, compiled for JDK 21, that must always build with Maven.

## ADDED Requirements

### Requirement: Maven quickstart project structure
The repository root SHALL contain a Maven project created from the Apache `maven-archetype-quickstart` archetype, with a `pom.xml` at the root, production sources under `src/main/java` and test sources under `src/test/java`.

#### Scenario: Standard layout present
- **WHEN** the repository root is inspected
- **THEN** `pom.xml`, `src/main/java` and `src/test/java` exist and the sources follow the quickstart archetype layout

### Requirement: JDK 21 build target
The Maven build SHALL compile production and test sources for Java 21.

#### Scenario: Compiler release is 21
- **WHEN** the effective build configuration in `pom.xml` is inspected
- **THEN** the Java compiler release is 21

### Requirement: Project compiles and packages with Maven
The project SHALL compile and package successfully with `mvn package`, running its tests without failures.

#### Scenario: Successful package build
- **WHEN** `mvn package` is executed in the repository root with a JDK supporting release 21
- **THEN** the build finishes with `BUILD SUCCESS` and all tests pass

### Requirement: Milestone commit for project initialization
The `pom.xml` and the basic project structure SHALL be committed to the repository in a commit whose message is exactly `[F0-C01] Initialize Maven project with JDK 21`.

#### Scenario: Mandatory commit exists
- **WHEN** the Git history of the branch is inspected
- **THEN** a commit with message exactly `[F0-C01] Initialize Maven project with JDK 21` exists and contains `pom.xml` and the basic project structure
