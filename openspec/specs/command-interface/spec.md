# command-interface Specification

## Purpose

Defines how the Amusement Park application receives text commands, splits them into fields with `;`, dispatches them to the matching operation, and handles incorrect commands without terminating.

## Requirements

### Requirement: Interactive command input
After printing its start banner, the application SHALL read commands from standard input, one command per line, process each line in order and print the resulting message, until standard input ends. Blank lines MUST be ignored.

#### Scenario: Commands processed in order
- **GIVEN** the application is started with two valid `USER CREATE` lines on standard input
- **WHEN** the input is processed
- **THEN** one result message is printed for each line, in input order

#### Scenario: End of input stops reading
- **GIVEN** the application is started with empty standard input
- **WHEN** it runs
- **THEN** it prints the start banner and finishes normally

### Requirement: Semicolon-separated command format
A command line SHALL consist of the command name followed by its fields, all separated by the `;` character, and MUST be interpreted with basic String operations including `split`, without external libraries. The command name is matched exactly, ignoring surrounding whitespace.

#### Scenario: Command name and fields are separated
- **GIVEN** the line `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **WHEN** it is interpreted
- **THEN** the command `USER CREATE` is executed with the seven fields `Ana`, `Garcia Lopez`, `12345678Z`, `ana@correo.com`, `Clave1!`, `612345678`, `1234567890123456`

### Requirement: Incorrect commands do not terminate the application
An unknown command or a command with the wrong number of fields SHALL produce an error message and MUST NOT terminate the application nor change any stored data; processing continues with the next line.

#### Scenario: Unknown command
- **GIVEN** the line `FOO BAR;1`
- **WHEN** it is processed
- **THEN** an error message reporting an unknown command is printed, no user is added, and the next line is still processed

#### Scenario: Wrong number of fields
- **GIVEN** the line `USER CREATE;Ana;Garcia Lopez`
- **WHEN** it is processed
- **THEN** an error message reporting the wrong number of fields is printed and no user is added
