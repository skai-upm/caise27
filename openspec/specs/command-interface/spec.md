# command-interface Specification

## Purpose

Defines how the application reads and interprets `;`-separated text commands from standard input or from a file, and how HELP, EXIT and invalid input behave.

## Requirements

### Requirement: Command syntax
Commands SHALL be lines whose elements are separated by `;`, interpreted with basic String operations including `split`. The recognised commands SHALL be `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`. Leading and trailing whitespace on a line SHALL be ignored.

#### Scenario: Unknown command
- **WHEN** the line `FOO;bar` is processed
- **THEN** an error is reported and the application continues accepting commands

### Requirement: Invalid input never terminates the application
Any malformed, unknown or rejected command SHALL produce an error message and SHALL NOT cause the application to terminate unexpectedly.

#### Scenario: Malformed command followed by valid one
- **WHEN** `USER CREATE;Ana` is processed followed by a valid `USER CREATE` command
- **THEN** the first produces an error and the second produces `USER CREATED`

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands from standard input, processing each and then waiting for the next, until `EXIT` or end of input.

#### Scenario: Interactive session
- **WHEN** the JAR is started with no arguments and receives `HELP` then `EXIT` on standard input
- **THEN** it prints the help text and then terminates normally

### Requirement: File mode
When started with a file path as first argument, the application SHALL process the commands in that file, one per line, in order, ignoring blank lines, using exactly the same command processing as interactive mode.

#### Scenario: Commands from file
- **WHEN** the JAR is run with a file containing two valid `USER CREATE` lines separated by a blank line and a `USER UPDATE` line
- **THEN** both users are created and the update is applied, in file order

#### Scenario: Missing or unreadable file
- **WHEN** the JAR is run with a path to a file that does not exist or cannot be read
- **THEN** an error message is shown and the application ends in a controlled way without a stack trace

### Requirement: HELP command
`HELP` SHALL display the available commands and their basic syntax, at least: `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP`, `EXIT`.

#### Scenario: Help lists commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of USER CREATE, USER DELETE, USER UPDATE, HELP and EXIT

### Requirement: EXIT command
`EXIT` SHALL terminate the application in a controlled manner; no command after `EXIT` SHALL be executed, in interactive or file mode.

#### Scenario: Commands after EXIT in a file are ignored
- **WHEN** a file contains `USER CREATE` (valid, user A), `EXIT`, `USER CREATE` (valid, user B)
- **THEN** user A is created and user B is not
