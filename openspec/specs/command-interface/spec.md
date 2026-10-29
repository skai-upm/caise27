# command-interface Specification

## Purpose

Defines how the application reads and interprets `;`-separated text commands from standard input or a command file, and how it reports help, exits, and handles malformed input.

## Requirements

### Requirement: Command parsing
Commands SHALL be lines whose elements are separated by `;`, interpreted with basic String operations including `split`, without external libraries or regular expressions. The application SHALL recognize `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`.

#### Scenario: Unknown command
- **WHEN** the line `FOO;BAR` is processed
- **THEN** an error message is printed and the application keeps running

### Requirement: Controlled error handling
No input, including malformed commands, missing fields or invalid values, SHALL cause the application to terminate unexpectedly.

#### Scenario: Malformed command
- **WHEN** the line `USER CREATE;Ana` is processed
- **THEN** an error message is printed and the next command is processed normally

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands from standard input, processing each command and then waiting for the next, until `EXIT` or end of input.

#### Scenario: Interactive session
- **WHEN** the JAR is run without arguments and `HELP` then `EXIT` are typed
- **THEN** the help text is printed and the application then terminates

### Requirement: File mode
When started with a file path as its first argument, the application SHALL process the commands in that file, one per line, in order, ignoring blank lines, with the same behavior as interactive mode. A nonexistent or unreadable file SHALL be reported with an error message, without an uncontrolled failure.

#### Scenario: Commands from file
- **WHEN** the JAR is run with a file containing two `USER CREATE` lines, a blank line and a `USER UPDATE` line
- **THEN** the commands are executed in order, the blank line is ignored, and the output matches running the same commands interactively

#### Scenario: Missing file
- **WHEN** the JAR is run with a path that does not exist
- **THEN** an error message is printed and the application terminates in a controlled manner

### Requirement: HELP command
`HELP` SHALL display the available commands and their basic syntax, covering at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** each of the five command syntaxes is printed

### Requirement: EXIT command
`EXIT` SHALL terminate the application in a controlled manner, and no command after it SHALL be executed, in either mode.

#### Scenario: Commands after EXIT ignored
- **WHEN** a command file contains `USER CREATE;...`, `EXIT`, `USER CREATE;...`
- **THEN** only the first `USER CREATE` is executed
