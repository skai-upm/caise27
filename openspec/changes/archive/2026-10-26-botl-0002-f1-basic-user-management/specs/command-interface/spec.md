# Spec Delta

## Purpose

Defines how the application receives API-like text commands from the terminal, splits them into operation and parameters, and reports results and errors.

## ADDED Requirements

### Requirement: Command parsing with semicolon-separated parameters
The system SHALL interpret each input line as one command whose components are separated by `;`. The first component identifies the operation (for example `USER CREATE`) and the remaining components are its parameters. Parsing SHALL use `String.split` and no external library.

#### Scenario: Recognised command is executed
- **WHEN** the line `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is received
- **THEN** the system executes the user-creation operation with the seven parameters

### Requirement: Terminal input
The system SHALL read commands line by line from standard input and process each line in order until the input ends.

#### Scenario: Several commands from the terminal
- **WHEN** two valid command lines are entered and standard input then ends
- **THEN** both commands are processed in order and the application terminates normally

### Requirement: Invalid commands do not terminate the application
The system SHALL report an error message and continue processing when it receives an unknown operation or a command with an incorrect number of parameters.

#### Scenario: Unknown operation
- **WHEN** the line `USER FLY;x` is received
- **THEN** the system reports an unknown-command error and keeps accepting commands

#### Scenario: Insufficient parameters
- **WHEN** the line `USER DELETE;ana@correo.com` is received
- **THEN** the system reports that the parameter count is wrong and no user is deleted

### Requirement: Operation results are reported
The system SHALL print a line starting with `OK` when an operation succeeds and a line starting with `ERROR` explaining the reason when it fails.

#### Scenario: Successful operation
- **WHEN** a valid `USER CREATE` command is processed
- **THEN** the output line starts with `OK`

#### Scenario: Failed operation
- **WHEN** a `USER CREATE` command with an invalid DNI is processed
- **THEN** the output line starts with `ERROR` and mentions the DNI
