# Spec Delta

## Purpose

Defines the text-command interface of the application: how each input line is split into elements, how the command name is recognized, and how incorrect commands are handled without terminating the application.

## ADDED Requirements

### Requirement: Semicolon-separated commands
Each input line SHALL be interpreted as one command whose elements are separated by the `;` character, the first element being the command name (for example `USER CREATE`). Interpretation SHALL use basic String operations, including `split`, and no external libraries. Surrounding whitespace of each element SHALL be ignored.

#### Scenario: Command elements are split on semicolons
- **WHEN** the line `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** it is interpreted as the command `USER CREATE` with the seven arguments `Ana`, `Garcia Lopez`, `12345678Z`, `ana@correo.com`, `Clave1!`, `612345678` and `1234567890123456`

### Requirement: Incorrect commands do not terminate the application
An unknown command, a command with the wrong number of arguments, or a command whose data is rejected SHALL produce a line starting with `ERROR:` and the application SHALL continue processing the following commands.

#### Scenario: Unknown command
- **WHEN** the line `FOO;bar` is processed
- **THEN** an `ERROR:` line reporting an unknown command is printed and the next command is processed normally

#### Scenario: Wrong number of arguments
- **WHEN** the line `USER CREATE;Ana` is processed
- **THEN** an `ERROR:` line reporting the wrong number of arguments is printed and no user is created

### Requirement: Blank lines are ignored
A line that is empty or contains only whitespace SHALL produce no output and SHALL NOT be treated as a command.

#### Scenario: Empty line
- **WHEN** an empty line is processed
- **THEN** nothing is printed
