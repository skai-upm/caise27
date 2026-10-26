# Spec Delta

## Purpose

Defines how the application receives text commands from the terminal, splits them into an operation and its parameters, and reports each result or error without terminating unexpectedly.

## ADDED Requirements

### Requirement: Terminal command input
The application SHALL read commands as text lines from standard input and SHALL process each non-blank line as one command, printing one response per command, until standard input ends.

#### Scenario: Commands processed line by line
- **WHEN** the application is started and receives two valid command lines on standard input
- **THEN** it processes them in order and prints one response for each

#### Scenario: End of input
- **WHEN** standard input reaches its end
- **THEN** the application terminates normally

### Requirement: Command splitting
Each command SHALL be split on the `;` character using `String.split`; the first element is the operation (for example `USER CREATE`) and the remaining elements are its parameters. Surrounding whitespace of each element SHALL be ignored and the operation SHALL be matched case-insensitively.

#### Scenario: Operation and parameters separated
- **WHEN** the command `USER DELETE;ana@correo.com;Clave1!` is received
- **THEN** the operation `USER DELETE` is executed with parameters `ana@correo.com` and `Clave1!`

### Requirement: Successful command response
A command that completes successfully SHALL print a line starting with `OK`.

#### Scenario: OK response
- **WHEN** a valid `USER CREATE` command is processed
- **THEN** the application prints `OK USER CREATED`

### Requirement: Invalid command handling
An unknown operation, or a known operation with a wrong number of parameters, SHALL be rejected with a line starting with `ERROR` describing the problem, and the application SHALL keep accepting further commands.

#### Scenario: Unknown command
- **WHEN** the command `FOO BAR;1` is received
- **THEN** the application prints an `ERROR` line indicating the command is unknown and continues reading commands

#### Scenario: Insufficient parameters
- **WHEN** the command `USER CREATE;Ana;Garcia` is received
- **THEN** the application prints an `ERROR` line indicating the wrong number of parameters, creates no user, and continues reading commands

#### Scenario: Unexpected failure does not terminate the application
- **WHEN** processing a command raises an unexpected error
- **THEN** the application prints an `ERROR` line and continues reading commands
