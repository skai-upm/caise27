# application-runtime Specification

## Purpose

Defines how the Amusement Park application is built and launched: an executable JAR produced by Maven that runs either interactively or over a command file.

## Requirements

### Requirement: Executable JAR build
The project SHALL be a standard Maven project targeting Java 21 that produces, through `mvn package`, a self-contained executable JAR whose manifest declares the application entry point. The JAR SHALL require only the Java standard library at runtime.

#### Scenario: Build and run without an IDE
- **WHEN** `mvn package` is executed on a clean checkout
- **THEN** an executable JAR is created under `target/`
- **AND** `java -jar target/<app>.jar` starts the application

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands line by line from standard input, process each one, and keep waiting for the next command until `EXIT` is received or input ends.

#### Scenario: Commands typed interactively
- **WHEN** the JAR is run without arguments and the user types `HELP` and then `EXIT`
- **THEN** the help text is displayed and the application terminates in a controlled manner

#### Scenario: End of input
- **WHEN** standard input is closed without an `EXIT` command
- **THEN** the application terminates in a controlled manner

### Requirement: Command file mode
When started with a file path as its first argument, the application SHALL process the commands in that file, one per line, in order, ignoring blank lines, with exactly the same behavior as interactive mode.

#### Scenario: File with several commands
- **WHEN** the JAR is run with `input.txt` containing two `USER CREATE` lines and a `USER UPDATE` line
- **THEN** the commands are executed in the order in which they appear and produce the same output as if typed interactively

#### Scenario: Blank lines
- **WHEN** the command file contains blank lines between commands
- **THEN** the blank lines are ignored and no error is reported for them

### Requirement: Missing or unreadable command file
The application SHALL handle a command file that does not exist or cannot be read by reporting an error message and terminating in a controlled manner, without an unhandled exception.

#### Scenario: Nonexistent file
- **WHEN** the JAR is run with a path to a file that does not exist
- **THEN** an error message naming the file is printed and the application exits without a stack trace
