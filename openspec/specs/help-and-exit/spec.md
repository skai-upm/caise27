# help-and-exit Specification

## Purpose

Provides the general control commands: `HELP` to show the available operations and their syntax, and `EXIT` to terminate processing in a controlled way in both input modes.

## Requirements

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`. (F1-C08)

#### Scenario: Help in interactive mode
- **WHEN** `HELP` is entered
- **THEN** the user commands and control commands above are listed with their syntax

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner in both interactive and file mode; in file mode no line after `EXIT` SHALL be processed. (F1-C08)

#### Scenario: Command after EXIT in a file
- **WHEN** a file contains `USER CREATE;...`, `EXIT` and another `USER CREATE;...`
- **THEN** only the first user is created and the second command is not executed

#### Scenario: EXIT interactively
- **WHEN** `EXIT` is entered on standard input
- **THEN** the application stops reading input and terminates normally
