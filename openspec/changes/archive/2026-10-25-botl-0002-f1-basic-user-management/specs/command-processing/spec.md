# Spec Delta

## Purpose

Defines how the application receives text commands with an API-like format, splits them into an operation and its parameters, dispatches them and reports malformed input without terminating.

## ADDED Requirements

### Requirement: Terminal command input
The application SHALL read commands from standard input, one command per line, and SHALL print exactly one response line per processed command. It SHALL keep reading until standard input ends.

#### Scenario: Several commands in sequence
- **WHEN** two valid `USER CREATE` lines are entered followed by end of input
- **THEN** two response lines are printed, one per command, and the application terminates normally

#### Scenario: Blank line
- **WHEN** an empty or whitespace-only line is entered
- **THEN** it is ignored and no response is printed for it

### Requirement: Command syntax
A command SHALL be a text line whose components are separated by `;`, interpreted with `String.split` and without regular-expression matching or external libraries. The first component is the operation name (for example `USER CREATE`); the remaining components are its parameters. The operation name is matched ignoring letter case and surrounding spaces, and each parameter is trimmed.

#### Scenario: Operation and parameters are separated
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the operation `USER DELETE` is executed with parameters `ana@correo.com` and `Clave1!`

### Requirement: Response format
Successful operations SHALL respond with a line starting with `OK`, and rejected operations with a line starting with `ERROR` followed by the reason.

#### Scenario: Successful and failed responses
- **WHEN** a valid `USER CREATE` is processed and then an identical one
- **THEN** the first response starts with `OK` and the second starts with `ERROR`

### Requirement: Invalid commands do not terminate the application
An unknown operation, or a known operation with fewer or more parameters than its format defines, SHALL be rejected with an `ERROR` response, and the application SHALL continue processing subsequent commands. No unhandled exception SHALL reach the user.

#### Scenario: Unknown operation
- **WHEN** the line `USER FLY;x` is processed
- **THEN** the response is `ERROR UNKNOWN COMMAND` and the next command is still processed

#### Scenario: Insufficient parameters
- **WHEN** the line `USER CREATE;Ana;Garcia Lopez` is processed
- **THEN** the response is `ERROR WRONG NUMBER OF PARAMETERS` and no user is created
