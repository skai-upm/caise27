# Spec Delta

## ADDED Requirements

### Requirement: Semicolon-separated commands
The elements of a command SHALL be separated by the `;` character, and commands SHALL be interpreted with basic String operations including `split`, without external libraries. The first element identifies the command (for example `USER CREATE`).

#### Scenario: Command name and arguments
- **WHEN** the line `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** it is handled as the `USER CREATE` command with the seven following elements as its arguments

### Requirement: Incorrect commands do not terminate the application
An incorrect command (unknown name, wrong number of elements or invalid values) SHALL be reported with an error message and SHALL NOT cause the application to terminate unexpectedly.

#### Scenario: Malformed command followed by a valid one
- **WHEN** `USER CREATE;Ana` is processed and then `HELP` is processed
- **THEN** an error is printed for the first line and the help text is printed for the second
