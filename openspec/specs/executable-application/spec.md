# executable-application Specification

## Purpose

Defines how the application is built into a standalone executable JAR and how it is launched from a terminal without an IDE (F0-C02).

## Requirements

### Requirement: Executable JAR packaging
`mvn package` SHALL produce `target/nombre-aplicacion.jar` whose manifest declares the application entry point, so that it starts with `java -jar` independently of any IDE.

#### Scenario: Package produces the executable JAR
- **WHEN** `mvn package` completes successfully
- **THEN** `target/nombre-aplicacion.jar` exists and its manifest declares a `Main-Class`

### Requirement: Launch modes
Run without arguments, the JAR SHALL start in interactive mode and wait for commands on standard input. Run with a text file path as its first argument, it SHALL process the commands contained in that file. From Delivery 2, `--fly` MAY be given as the first argument, alone (interactive) or followed by a command file path, to run with temporary persistence.

#### Scenario: Interactive launch
- **WHEN** `java -jar target/nombre-aplicacion.jar` is run
- **THEN** the application starts and reads commands from standard input

#### Scenario: File launch
- **WHEN** `java -jar target/nombre-aplicacion.jar input.txt` is run with a readable command file
- **THEN** the application processes the commands contained in `input.txt`

#### Scenario: Fly launches
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly` or `java -jar target/nombre-aplicacion.jar --fly input_fase2.txt` is run
- **THEN** the application runs interactively or processes the file, respectively, using temporary persistence
