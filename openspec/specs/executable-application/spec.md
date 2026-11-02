# executable-application Specification

## Purpose

Packages the application as a standalone executable JAR that runs from a terminal without an IDE, in interactive mode or with a command file argument.

## Requirements

### Requirement: Executable JAR packaging
`mvn package` SHALL produce `target/nombre-aplicacion.jar` with a valid main entry point, so that the application starts with `java -jar target/nombre-aplicacion.jar` without an IDE, extra classpath or additional configuration.

#### Scenario: Interactive start
- **WHEN** `java -jar target/nombre-aplicacion.jar` is run after a successful `mvn package`
- **THEN** the application starts in interactive mode and waits for commands on standard input

#### Scenario: Command file start
- **WHEN** `java -jar target/nombre-aplicacion.jar input.txt` is run with a readable text command file
- **THEN** the application processes the commands contained in `input.txt` through the same command processing used in interactive mode
