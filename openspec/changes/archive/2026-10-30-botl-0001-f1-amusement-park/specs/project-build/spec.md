# Spec Delta

## Purpose

Defines how the Amusement Park application is structured, built with Maven for JDK 21, and packaged as an executable JAR that runs independently of any IDE.

## ADDED Requirements

### Requirement: Standard Maven project for JDK 21
The project SHALL be a Maven project with the standard apache-maven-quickstart structure (`pom.xml`, `src/main/java`, `src/test/java`) configured to compile for Java 21.

#### Scenario: Project compiles with Maven
- **WHEN** `mvn package` is run on a fresh checkout with a JDK of version 21 or later
- **THEN** the build succeeds and the compiled classes target Java 21

### Requirement: Executable JAR generation
The build SHALL produce an executable JAR at `target/amusement-park.jar` whose manifest declares the application entry-point class, so that it can be started with `java -jar` without an IDE or extra classpath configuration.

#### Scenario: JAR starts in interactive mode
- **WHEN** `java -jar target/amusement-park.jar` is run without arguments
- **THEN** the application starts and waits for commands on standard input

#### Scenario: JAR starts in file mode
- **WHEN** `java -jar target/amusement-park.jar input.txt` is run
- **THEN** the application processes the commands contained in `input.txt`

### Requirement: Standard library only at runtime
The application SHALL use only the Java standard library at runtime; it SHALL NOT use databases, frameworks, external libraries, regular expressions or encryption mechanisms.

#### Scenario: Packaged JAR has no third-party runtime dependencies
- **WHEN** the project's runtime dependencies are inspected
- **THEN** no compile- or runtime-scoped dependency other than the JDK is declared
