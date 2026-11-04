# Spec Delta

## Purpose

Defines how the terminal application receives API-like text commands, splits them into operation and parameters with `String.split`, and handles invalid input without terminating.

## ADDED Requirements

### Requirement: Terminal command input
The application SHALL read commands line by line from standard input and print one result message per non-blank command, continuing until the input ends.

#### Scenario: Command sequence from the terminal
- **WHEN** several valid commands are entered one per line followed by end of input
- **THEN** one result message is printed per command and the application exits normally

### Requirement: Command splitting
A command SHALL be split on `;` with `String.split` into the operation and its parameters, and the operation SHALL be split on spaces into its words (for example `USER` and `CREATE`); surrounding whitespace of each part SHALL be ignored.

#### Scenario: Parameters extracted
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is handled as the user-delete operation with email `ana@correo.com` and password `Clave1!`

### Requirement: Safe rejection of invalid commands
The application SHALL respond with an error message, without terminating and without creating or modifying any user, when a command is unknown, has fewer or more parameters than its format requires, or has an unsupported operation.

#### Scenario: Unknown or malformed commands
- **WHEN** `FOO`, `USER LIST`, `USER CREATE;Ana`, `USER DELETE;a@b.com` or an empty-parameter command is processed
- **THEN** an error message is printed, the application keeps accepting commands, and no user is created or changed
