# project-build Specification

## Purpose
Defines the build contract of the Amusement Park project: a Maven project with the quickstart archetype structure that targets JDK 21 and compiles with Maven.

## Requirements

### Requirement: Maven quickstart project structure
The project SHALL be a Maven project whose layout follows the Apache `maven-archetype-quickstart` archetype: a `pom.xml` at the repository root, production sources under `src/main/java` and test sources under `src/test/java`.

#### Scenario: Standard layout present
- **WHEN** the repository root is inspected
- **THEN** it contains `pom.xml`, a `src/main/java` directory with the application sources and a `src/test/java` directory with the test sources

### Requirement: JDK 21 target
The project SHALL be configured to compile for Java 21 through the `pom.xml`.

#### Scenario: Compiler release is 21
- **WHEN** the effective `pom.xml` configuration is inspected
- **THEN** the Java compiler release (or source and target) is set to 21

#### Scenario: Compiled classes target Java 21
- **WHEN** the project is compiled with Maven
- **THEN** the generated class files have the Java 21 class-file version (major version 65)

### Requirement: Compilable with Maven
The project SHALL compile, run its tests and package successfully using Maven.

#### Scenario: Successful build
- **WHEN** `mvn package` is executed from the repository root
- **THEN** the build finishes with `BUILD SUCCESS`, the tests pass and a JAR is produced in `target/`

### Requirement: Mandatory milestone commit
The initial Maven structure SHALL be committed to the repository with the exact commit message `[F0-C01] Initialize Maven project with JDK 21`.

#### Scenario: Milestone commit exists
- **WHEN** the Git history of the delivery branch is inspected
- **THEN** a commit with message exactly `[F0-C01] Initialize Maven project with JDK 21` exists and contains `pom.xml` and the basic source structure
