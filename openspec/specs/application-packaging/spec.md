# application-packaging Specification

## Purpose

Defines how the Amusement Park application is built and packaged with Maven on JDK 21 as an executable JAR, and the technical restrictions the build must respect.

## Requirements

### Requirement: Maven project targeting JDK 21
The project SHALL be a Maven project with the standard Maven (quickstart archetype) directory layout, configured to compile for Java 21, and SHALL compile with Maven.

#### Scenario: Project compiles with Maven
- **WHEN** `mvn package` is run on a clean checkout with a JDK 21+ toolchain
- **THEN** the build succeeds and compiled classes target Java 21

#### Scenario: Standard layout
- **WHEN** the repository is inspected
- **THEN** it contains `pom.xml`, production sources under `src/main/java` and tests under `src/test/java`

### Requirement: Executable JAR
`mvn package` SHALL produce `target/nombre-aplicacion.jar` whose manifest declares the application entry point, so it can be started with `java -jar target/nombre-aplicacion.jar` without an IDE or extra configuration.

#### Scenario: JAR starts without arguments
- **WHEN** `java -jar target/nombre-aplicacion.jar` is run
- **THEN** the application starts in interactive mode and waits for commands on standard input

#### Scenario: JAR starts with a command file
- **WHEN** `java -jar target/nombre-aplicacion.jar input.txt` is run
- **THEN** the application processes the commands contained in `input.txt`

### Requirement: Standard library only at runtime
The application SHALL use only the Java standard library and the BCrypt library `org.mindrot:jbcrypt:0.4` at runtime, bundled into the executable JAR: no databases, frameworks, other external libraries or regular expressions.

#### Scenario: No runtime dependencies
- **WHEN** the project's dependencies are inspected
- **THEN** the only dependency not in `test` scope is `org.mindrot:jbcrypt:0.4`

#### Scenario: Self-contained JAR
- **WHEN** `java -jar target/nombre-aplicacion.jar` is run with no extra classpath
- **THEN** password hashing works without missing-class errors
