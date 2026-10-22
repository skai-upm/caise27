# command-interface Specification

## Purpose

Defines the textual command language of the application: how commands are written and interpreted, how errors are reported, and the general HELP and EXIT commands.

## Requirements

### Requirement: Command syntax
Every command SHALL be a single line whose elements are separated by `;`. The first element SHALL be the command name (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`), followed by its arguments. Interpretation SHALL use basic String operations, without regular expressions or external libraries.

#### Scenario: Recognized command
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is interpreted as the `USER DELETE` command with arguments `ana@correo.com` and `Clave1!`

### Requirement: Robust handling of invalid input
An unknown command, a command with the wrong number of arguments, or a command whose data is rejected SHALL produce an error message and SHALL NOT terminate the application; processing SHALL continue with the next command.

#### Scenario: Unknown command
- **WHEN** the line `FOO BAR;1` is processed
- **THEN** an error message is displayed and the next command is still processed

#### Scenario: Wrong number of arguments
- **WHEN** the line `USER CREATE;Ana;Garcia` is processed
- **THEN** an error message describing the expected syntax is displayed and no user is created

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, covering at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.

#### Scenario: Show help
- **WHEN** `HELP` is processed
- **THEN** each available command and its syntax is printed

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner. No command after `EXIT` SHALL be executed, in either interactive or file mode.

#### Scenario: Commands after EXIT in a file
- **WHEN** a command file contains `USER CREATE;...`, then `EXIT`, then another `USER CREATE;...`
- **THEN** only the first user is created and the line after `EXIT` is not executed
