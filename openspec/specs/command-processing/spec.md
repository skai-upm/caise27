# command-processing Specification

## Purpose

Defines how the application receives text commands with an API-like format, splits them into an operation and parameters, and reports malformed commands without terminating.

## Requirements

### Requirement: Line-based command input
The application SHALL read commands from standard input, treating each line as one command, and SHALL print one response line for each processed command. Blank lines SHALL be ignored.

#### Scenario: Command typed in the terminal
- **WHEN** the user types `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the application processes the command and prints its result

#### Scenario: Blank line
- **WHEN** the user enters an empty line
- **THEN** nothing is executed and no error is reported

### Requirement: Command syntax
A command SHALL consist of fields separated by `;`. The first field SHALL be the operation name (for example `USER CREATE`) and the remaining fields its parameters, interpreted positionally.

#### Scenario: Operation and parameters
- **WHEN** the command `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the operation `USER DELETE` is executed with parameters `ana@correo.com` and `Clave1!`

### Requirement: Malformed commands are reported
The application SHALL respond with a line starting with `ERROR` when a command is unknown or has fewer or more parameters than its syntax requires, and SHALL continue accepting commands afterwards.

#### Scenario: Unknown command
- **WHEN** the command `FOO BAR;1` is processed
- **THEN** the application prints an `ERROR` line stating the command is unknown and keeps running

#### Scenario: Insufficient parameters
- **WHEN** the command `USER CREATE;Ana;Garcia Lopez` is processed
- **THEN** the application prints an `ERROR` line indicating the expected syntax, no user is created, and the application keeps running
