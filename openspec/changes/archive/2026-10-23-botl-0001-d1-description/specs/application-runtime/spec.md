# Spec Delta

## Purpose

Defines how the Amusement Park application is built into an executable JAR and how it is started, either interactively from standard input or with a command file.

## ADDED Requirements

### Requirement: Executable JAR build
The project SHALL be a standard Maven project targeting Java 21 that produces an executable JAR with `mvn package`, runnable with `java -jar` without an IDE or extra configuration, and using only the Java standard library at runtime.

#### Scenario: Build and run the JAR
- **WHEN** `mvn package` is run on a clean checkout
- **THEN** the build succeeds and produces `target/amusement-park.jar`
- **AND** `java -jar target/amusement-park.jar` starts the application

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands line by line from standard input, process each one, and keep waiting for the next command until `EXIT` is received or input ends.

#### Scenario: Commands from standard input
- **WHEN** the JAR is started without arguments and the user types `HELP` followed by `EXIT`
- **THEN** the help text is printed and the application then terminates normally

#### Scenario: End of input
- **WHEN** standard input is closed without an `EXIT` command
- **THEN** the application terminates normally

### Requirement: File mode
When started with a file path as first argument, the application SHALL process the commands in that file, one per line, in order, ignoring blank lines, with exactly the same observable behaviour per command as in interactive mode.

#### Scenario: Commands from a file
- **WHEN** the JAR is run with `input.txt` containing two `USER CREATE` lines and an `EXIT` line
- **THEN** both users are created in file order and the application terminates

#### Scenario: Blank lines ignored
- **WHEN** the command file contains empty or whitespace-only lines between commands
- **THEN** those lines produce no output and no error

### Requirement: Missing or unreadable command file
The application SHALL handle a command file that does not exist or cannot be read in a controlled manner: it prints an error message naming the file and exits without a stack trace and with a non-zero exit status.

#### Scenario: Nonexistent file
- **WHEN** the JAR is run with `missing.txt` as argument and that file does not exist
- **THEN** an error message mentioning `missing.txt` is printed and the application exits with a non-zero status without processing any command
