# Spec Delta

## Purpose

Defines how the amusement park application is built, started and driven: an executable Java 21 / Maven JAR that reads `;`-separated text commands interactively or from a command file and processes them identically.

## ADDED Requirements

### Requirement: Executable JAR build
The project SHALL be a standard Maven project targeting Java 21 that builds with `mvn package` and produces an executable JAR in `target/` that starts with `java -jar` without an IDE, extra classpath entries or source changes. Only the Java standard library SHALL be used at runtime.

#### Scenario: Build and start the JAR
- **WHEN** `mvn package` is run on a clean checkout and the produced JAR is started with `java -jar target/<app>.jar`
- **THEN** the build succeeds and the application starts and waits for commands on standard input

### Requirement: Command syntax
The application SHALL interpret each input line as a command whose elements are separated by the `;` character. The first element SHALL identify the command (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`). Command keywords SHALL be recognised regardless of letter case and surrounding whitespace. Parsing SHALL use basic String operations and `split`, without regular expressions or external libraries.

#### Scenario: Recognised command
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is handled as the `USER DELETE` command with arguments `ana@correo.com` and `Clave1!`

#### Scenario: Unknown command
- **WHEN** the line `FOO BAR;1;2` is processed
- **THEN** the application reports an error message stating the command is unknown and continues running

#### Scenario: Wrong number of arguments
- **WHEN** the line `USER DELETE;ana@correo.com` is processed
- **THEN** the application reports an error message showing the expected syntax, changes no data and continues running

### Requirement: Robust command processing
An incorrect, malformed or rejected command SHALL NOT terminate the application. Each command SHALL produce one human-readable result line; rejected commands SHALL produce a line starting with `ERROR`.

#### Scenario: Invalid input does not crash
- **WHEN** a sequence contains an invalid command followed by a valid one
- **THEN** an error is reported for the invalid command and the valid command is still executed

### Requirement: Interactive mode
When the JAR is started without arguments, the application SHALL read commands from standard input one line at a time, process each one, and wait for the next until `EXIT` is processed or standard input ends.

#### Scenario: Interactive session
- **WHEN** the JAR is started without arguments and the user types a valid `USER CREATE` command followed by `EXIT`
- **THEN** the application prints `USER CREATED`, then terminates normally after `EXIT`

### Requirement: File mode
When the JAR is started with a file path as its first argument, the application SHALL process the commands contained in that file, one command per line, in order of appearance, ignoring blank lines. Command handling SHALL be identical to interactive mode. A nonexistent or unreadable file SHALL be reported with an error message and SHALL NOT produce an uncontrolled failure (no stack trace).

#### Scenario: Commands executed in order
- **WHEN** the JAR is started with a file containing a `USER CREATE` line, a blank line and a `USER UPDATE` line for the created user
- **THEN** the user is created and then updated, and the blank line is ignored

#### Scenario: Missing command file
- **WHEN** the JAR is started with the path of a file that does not exist
- **THEN** the application prints an error message naming the file and exits with a non-zero status without a stack trace

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, covering at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.

#### Scenario: Show help
- **WHEN** the command `HELP` is processed
- **THEN** the syntax of every available command is printed and the application keeps running

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner. In file mode no line after `EXIT` SHALL be processed.

#### Scenario: Lines after EXIT are ignored
- **WHEN** a command file contains `USER CREATE` for user A, then `EXIT`, then `USER CREATE` for user B
- **THEN** user A is created, the application terminates, and user B is not created
