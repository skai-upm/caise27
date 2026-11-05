# project-initialization Specification

## Purpose
Defines the build baseline of the Amusement Park application: a Maven project created from the quickstart archetype, targeting JDK 21, that compiles with Maven.

## Requirements

### Requirement: Maven quickstart project structure
The repository SHALL contain a Maven project whose root `pom.xml` and directory layout follow the Apache `maven-archetype-quickstart` archetype: production sources under `src/main/java` and test sources under `src/test/java`.

#### Scenario: Standard structure present on a clean checkout
- **GIVEN** a clean checkout of the repository
- **WHEN** the project root is inspected
- **THEN** a `pom.xml` file exists at the root
- **AND** the directories `src/main/java` and `src/test/java` exist

#### Scenario: Non-Maven layout is not used
- **GIVEN** a clean checkout of the repository
- **WHEN** the source tree is inspected
- **THEN** no Java production sources exist outside `src/main/java`

### Requirement: JDK 21 build target
The Maven build SHALL be configured to compile sources for Java release 21.

#### Scenario: Compiler release set to 21
- **GIVEN** the project `pom.xml`
- **WHEN** the effective compiler configuration is inspected
- **THEN** the Java release (or source/target) level is 21

#### Scenario: Language level above 21 is not required
- **GIVEN** a JDK 21 toolchain
- **WHEN** the project is compiled
- **THEN** compilation does not fail because of an unsupported release level

### Requirement: Project compiles with Maven
The project SHALL compile and run its tests successfully with Maven on a clean checkout using JDK 21 or newer.

#### Scenario: Successful build
- **GIVEN** a clean checkout, JDK 21 and Maven available
- **WHEN** `mvn -q test` (or `mvn compile`) is executed at the repository root
- **THEN** the build finishes successfully with no compilation errors

#### Scenario: Build outputs are not versioned
- **GIVEN** the project has been built
- **WHEN** the working tree status is inspected
- **THEN** the `target/` build output directory is ignored by Git
