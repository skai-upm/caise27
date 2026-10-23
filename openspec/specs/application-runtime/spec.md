# application-runtime Specification

## Purpose

Defines how the Amusement Park application is built into an executable JAR and how it is started, either interactively from standard input or with a command file.

## Requirements

### Requirement: Executable JAR build
The project SHALL be a standard Maven project targeting Java 21 that produces an executable JAR with `mvn package`, runnable with `java -jar` without an IDE or extra configuration. At runtime it SHALL use only the Java standard library plus the BCrypt library, which SHALL be declared as a Maven dependency and bundled inside the JAR.

#### Scenario: Build and run the JAR
- **WHEN** `mvn package` is run on a clean checkout
- **THEN** the build succeeds and produces `target/amusement-park.jar`
- **AND** `java -jar target/amusement-park.jar` starts the application

#### Scenario: Self-contained JAR
- **WHEN** only `target/amusement-park.jar` is copied to another directory and run with `java -jar`
- **THEN** creating a user (which hashes the password) works

### Requirement: Interactive mode
When started without a command file (no arguments, or only `--fly`), the application SHALL read commands line by line from standard input, process each one, and keep waiting for the next command until `EXIT` is received or input ends.

#### Scenario: Commands from standard input
- **WHEN** the JAR is started without arguments and the user types `HELP` followed by `EXIT`
- **THEN** the help text is printed and the application then terminates normally

#### Scenario: End of input
- **WHEN** standard input is closed without an `EXIT` command
- **THEN** the application terminates normally

#### Scenario: Interactive temporary mode
- **WHEN** the JAR is started with `--fly` as the only argument
- **THEN** commands are read from standard input using temporary persistence

### Requirement: File mode
When started with a command file path as first argument, or as second argument after `--fly`, the application SHALL process the commands in that file, one per line, in order, ignoring blank lines, with exactly the same observable behaviour per command as in interactive mode.

#### Scenario: Commands from a file
- **WHEN** the JAR is run with `input.txt` containing two `USER CREATE` lines and an `EXIT` line
- **THEN** both users are created in file order and the application terminates

#### Scenario: Blank lines ignored
- **WHEN** the command file contains empty or whitespace-only lines between commands
- **THEN** those lines produce no output and no error

#### Scenario: File in temporary mode
- **WHEN** the JAR is run with `--fly input_fase2.txt`
- **THEN** the commands in `input_fase2.txt` are processed using temporary persistence

### Requirement: Missing or unreadable command file
The application SHALL handle a command file that does not exist or cannot be read in a controlled manner: it prints an error message naming the file and exits without a stack trace and with a non-zero exit status.

#### Scenario: Nonexistent file
- **WHEN** the JAR is run with `missing.txt` as argument and that file does not exist
- **THEN** an error message mentioning `missing.txt` is printed and the application exits with a non-zero status without processing any command

### Requirement: Temporary persistence mode
When `--fly` is the first argument, the application SHALL create a new, empty directory `tmp/<yyyyMMdd_HHmmss>` (made unique if it already exists) and use it as the data directory for all persistent elements (users, groups, tickets, passes, attractions) during that execution. When execution ends — through `EXIT`, end of the command file or end of standard input — the directory and all its files SHALL be deleted, together with `tmp/` if it was created by this execution and is left empty. Without `--fly`, the normal data directory is used and argument handling is unchanged.

#### Scenario: Persistent data untouched
- **WHEN** `users.txt` exists in the working directory and the JAR is run with `--fly` and a file that creates users
- **THEN** the users are created during that run, the existing `users.txt` is unchanged, and no temporary files remain after it ends

#### Scenario: Fresh state
- **WHEN** the JAR is run with `--fly`
- **THEN** it starts with no users, groups, tickets, passes or attractions
