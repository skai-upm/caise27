# Spec Delta

## Purpose

Defines how the application is built into a standalone executable JAR and how it is launched from a terminal without an IDE (F0-C02).

## ADDED Requirements

### Requirement: Executable JAR packaging
`mvn package` SHALL produce `target/nombre-aplicacion.jar` whose manifest declares the application entry point, so that it starts with `java -jar` independently of any IDE.

#### Scenario: Package produces the executable JAR
- **WHEN** `mvn package` completes successfully
- **THEN** `target/nombre-aplicacion.jar` exists and its manifest declares a `Main-Class`

### Requirement: Launch modes
Run without arguments, the JAR SHALL start in interactive mode and wait for commands on standard input. Run with a text file path as its first argument, it SHALL process the commands contained in that file.

#### Scenario: Interactive launch
- **WHEN** `java -jar target/nombre-aplicacion.jar` is run
- **THEN** the application starts and reads commands from standard input

#### Scenario: File launch
- **WHEN** `java -jar target/nombre-aplicacion.jar input.txt` is run with a readable command file
- **THEN** the application processes the commands contained in `input.txt`
