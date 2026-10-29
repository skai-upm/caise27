# project-build Specification

## Purpose

Defines the Maven project structure, Java version and build guarantees of the Amusement Park application so that every later delivery builds on an accepted, compilable project.

## Requirements

### Requirement: Maven quickstart project structure
The repository SHALL contain a Maven project created from the apache-maven-quickstart archetype, with a `pom.xml` at the project root, production sources under `src/main/java` and test sources under `src/test/java`. Sources SHALL NOT be placed in the default Java package.

#### Scenario: Standard layout present
- **WHEN** the repository root is inspected
- **THEN** it contains `pom.xml`, a `src/main/java` directory with at least one Java class inside a named package, and a `src/test/java` directory with at least one test class

### Requirement: JDK 21 configuration
The Maven project SHALL be configured to compile Java sources for JDK 21.

#### Scenario: Compiler targets Java 21
- **WHEN** the `pom.xml` is inspected
- **THEN** the compiler release (or equivalent source/target configuration) is set to 21

#### Scenario: Compiled classes are Java 21 bytecode
- **WHEN** the project is compiled with Maven
- **THEN** the produced class files have the Java 21 class-file major version (65)

### Requirement: Compilable with Maven
The project SHALL compile and pass its tests using Maven without an IDE or additional configuration.

#### Scenario: Successful package build
- **WHEN** `mvn package` is run from the repository root
- **THEN** the build finishes with `BUILD SUCCESS` and all tests pass
