# help-and-exit Specification

## Purpose

Provides the general `HELP` command that lists available commands and their syntax, and the `EXIT` command that terminates processing in a controlled manner in both input modes.

## Requirements

### Requirement: HELP command
`HELP` SHALL display the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.

#### Scenario: Interactive help
- **WHEN** `HELP` is entered
- **THEN** the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT` is displayed and the application keeps waiting for commands

### Requirement: EXIT command
`EXIT` SHALL terminate the application in a controlled manner in interactive and file mode; in file mode no line after `EXIT` SHALL be processed.

#### Scenario: Interactive exit
- **WHEN** `EXIT` is entered interactively
- **THEN** the application stops reading commands and terminates normally

#### Scenario: Commands after EXIT in a file
- **WHEN** a file containing `USER CREATE;...`, then `EXIT`, then another `USER CREATE;...` is processed
- **THEN** the first user is created and the second `USER CREATE` is not executed
