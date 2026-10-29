# Spec Delta

## Purpose

Defines how the Amusement Park application is structured as a Maven project, which JDK it targets, and how it is packaged and launched as an executable JAR.

## ADDED Requirements

### Requirement: Standard Maven project targeting JDK 21
The project SHALL be a Maven project with the standard Maven directory structure (as produced by the maven-quickstart archetype) and SHALL compile for Java 21.

#### Scenario: Project compiles with Maven
- **WHEN** `mvn package` is run on a clean checkout
- **THEN** the build succeeds and compiles the sources for Java release 21

### Requirement: Executable JAR
`mvn package` SHALL produce an executable JAR in `target/` that declares the application entry-point class in its manifest, so it can be started with `java -jar` without an IDE or additional configuration.

#### Scenario: JAR runs without arguments
- **WHEN** the user runs `java -jar target/amusement-park.jar`
- **THEN** the application starts in interactive mode and waits for commands on standard input

#### Scenario: JAR runs with a command file
- **WHEN** the user runs `java -jar target/amusement-park.jar input.txt`
- **THEN** the application processes the commands contained in `input.txt`

### Requirement: Standard library only at runtime
The application SHALL use only the Java standard library at runtime and SHALL NOT use databases, frameworks, external libraries, regular expressions, or encryption mechanisms.

#### Scenario: No runtime dependencies
- **WHEN** the project's dependency tree is inspected
- **THEN** it contains no compile- or runtime-scope dependencies (test-scope dependencies are permitted)
