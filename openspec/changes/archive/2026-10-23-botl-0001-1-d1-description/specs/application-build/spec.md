# Spec Delta

## Purpose

Defines how the amusement park application is built from source and launched from a terminal, independently of any IDE.

## ADDED Requirements

### Requirement: Maven build on Java 21
The project SHALL be a Maven project with the standard Maven directory layout, compiled for Java 21, and SHALL build successfully with `mvn package` from a clean checkout.

#### Scenario: Clean build succeeds
- **WHEN** `mvn package` is run in the project root of a clean checkout with JDK 21 or newer
- **THEN** the build completes successfully and compiles sources targeting Java release 21

### Requirement: Executable JAR
`mvn package` SHALL produce a self-contained executable JAR in `target/` that declares the application entry point in its manifest and can be started with `java -jar` without any IDE or extra classpath configuration.

#### Scenario: JAR starts with java -jar
- **WHEN** the user runs `java -jar target/amusement-park.jar` after `mvn package`
- **THEN** the application starts without class-loading or manifest errors

### Requirement: Runtime uses only the Java standard library
The application SHALL depend only on the Java standard library at runtime. It SHALL NOT use databases, frameworks, external libraries, regular expressions, or encryption mechanisms.

#### Scenario: No runtime dependencies packaged
- **WHEN** the built JAR is inspected
- **THEN** it contains only the application's own classes and resources

### Requirement: Execution mode selection
When started without arguments, the application SHALL run in interactive mode and read commands from standard input. When started with at least one argument, it SHALL treat the first argument as the path of a command file and run in file mode.

#### Scenario: No arguments selects interactive mode
- **WHEN** the application is started with no arguments
- **THEN** it waits for commands on standard input

#### Scenario: One argument selects file mode
- **WHEN** the application is started as `java -jar target/amusement-park.jar input.txt`
- **THEN** it processes the commands contained in `input.txt`
