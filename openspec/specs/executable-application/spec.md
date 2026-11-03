# executable-application Specification

## Purpose

Makes the application buildable with `mvn package` and runnable from a terminal with `java -jar`, without an IDE, in interactive or file mode.

## Requirements

### Requirement: Executable JAR packaging
Running `mvn package` SHALL produce `target/nombre-aplicacion.jar` whose manifest declares the application entry point, so that it starts with `java -jar` without an IDE or extra classpath configuration.

#### Scenario: JAR is produced
- **WHEN** `mvn package` completes successfully
- **THEN** `target/nombre-aplicacion.jar` exists and its manifest names the main class

#### Scenario: Interactive start
- **WHEN** `java -jar target/nombre-aplicacion.jar` is run without arguments
- **THEN** the application starts and reads commands from standard input

### Requirement: Command file argument
When a text file path is given as the first argument, the JAR SHALL process the commands contained in that file through the same command processing used for standard input.

#### Scenario: File mode start
- **WHEN** `java -jar target/nombre-aplicacion.jar input.txt` is run with a readable command file
- **THEN** the commands in `input.txt` are processed
