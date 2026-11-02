# Spec Delta

## Purpose

Makes the application buildable with `mvn package` into a standalone JAR that starts with `java -jar`, in interactive mode or with a command file, independently of any IDE.

## ADDED Requirements

### Requirement: Executable JAR produced by Maven
`mvn package` SHALL produce `target/nombre-aplicacion.jar` whose manifest declares the application entry point, so that `java -jar target/nombre-aplicacion.jar` starts the application without an IDE or extra classpath configuration. (F0-C02)

#### Scenario: Build and start interactively
- **WHEN** `mvn package` succeeds and `java -jar target/nombre-aplicacion.jar` is run with no arguments
- **THEN** the application starts and waits for commands on standard input

#### Scenario: Start with a command file
- **WHEN** `java -jar target/nombre-aplicacion.jar input.txt` is run with a readable text file
- **THEN** the application processes the commands contained in `input.txt` through the same command processing used interactively
