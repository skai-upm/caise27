# command-interface Specification

## Purpose

Defines the text command interface of the application: how each input line is split into elements with the `;` separator, dispatched to an operation, and how unrecognised input is handled without terminating the application.

## Requirements

### Requirement: Semicolon-separated commands
Each input line SHALL be interpreted as one command whose elements are separated by the `;` character; the first element identifies the command (for example `USER CREATE`). Interpretation MUST use basic String operations including `split`, without external libraries. Leading and trailing whitespace around each element SHALL be ignored, and blank lines SHALL be ignored.

#### Scenario: Command elements are separated by semicolons
- **WHEN** the line `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** it is recognised as the `USER CREATE` command with the seven data elements in order

#### Scenario: Blank line ignored
- **WHEN** an empty line is processed
- **THEN** no output is produced and the application keeps waiting for commands

### Requirement: Unrecognised commands do not terminate the application
A line whose first element is not a recognised command SHALL produce an error message starting with `ERROR` and the application SHALL continue processing subsequent commands.

#### Scenario: Unknown command reported
- **WHEN** the line `FOO;bar` is processed
- **THEN** an error message starting with `ERROR` is printed
- **AND** the next valid command is still processed normally

#### Scenario: Wrong number of elements reported
- **WHEN** the line `USER CREATE;Ana;Garcia` is processed
- **THEN** an error message starting with `ERROR` is printed, no user is created and the application keeps running
