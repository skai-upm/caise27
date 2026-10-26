# Spec Delta

## Purpose

Defines how the terminal application receives API-like text commands, splits them into operation and parameters, and reports malformed input without stopping.

## ADDED Requirements

### Requirement: Terminal command input
The application SHALL read commands from the terminal, one command per line, and SHALL print a response line for every non-empty command until the input ends.

#### Scenario: Command processed from terminal
- **WHEN** the user types `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the application prints a success message and waits for the next command

#### Scenario: Input ends
- **WHEN** the terminal input reaches end of stream
- **THEN** the application terminates normally

### Requirement: Command format and splitting
A command SHALL consist of an operation followed by parameters separated by `;`. The operation SHALL be two words separated by a space (domain and action, e.g. `USER CREATE`). Commands SHALL be interpreted using `String.split`, without regular-expression-based matching or external libraries.

#### Scenario: Operation and parameters recognised
- **WHEN** the command `USER DELETE;ana@correo.com;Clave1!` is received
- **THEN** the operation is identified as `USER DELETE` with parameters `ana@correo.com` and `Clave1!`

### Requirement: Invalid commands do not terminate the program
The application SHALL respond with an error message, and continue accepting commands, when a command is unknown, has fewer or more parameters than its format requires, or has otherwise unparseable content.

#### Scenario: Unknown command
- **WHEN** the command `USER LIST` is received
- **THEN** the application prints an error indicating the command is unknown and keeps running

#### Scenario: Insufficient parameters
- **WHEN** the command `USER CREATE;Ana;Garcia` is received
- **THEN** the application prints an error indicating the wrong number of parameters, no user is created, and the application keeps running

#### Scenario: Empty line
- **WHEN** an empty or blank line is received
- **THEN** it is ignored and the application keeps running
