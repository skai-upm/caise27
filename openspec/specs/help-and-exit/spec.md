# help-and-exit Specification

## Purpose

Provides the general control commands: `HELP` to list available commands and their syntax, and `EXIT` to terminate in a controlled way.

## Requirements

### Requirement: HELP command
`HELP` SHALL display the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is entered
- **THEN** each of the five commands and its syntax is displayed

### Requirement: EXIT command
`EXIT` SHALL terminate command processing in a controlled manner in both interactive and file mode; in file mode no line after `EXIT` SHALL be processed.

#### Scenario: Interactive exit
- **WHEN** `EXIT` is entered interactively
- **THEN** the application stops reading input and terminates normally

#### Scenario: Commands after EXIT are not executed
- **WHEN** a file contains `USER CREATE;...`, then `EXIT`, then another `USER CREATE;...`
- **THEN** only the first user is created
