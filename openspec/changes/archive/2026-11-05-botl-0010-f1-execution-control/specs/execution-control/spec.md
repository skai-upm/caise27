# Spec Delta

## Purpose

Provides the general application commands `HELP`, which lists the available operations and their syntax, and `EXIT`, which terminates the application in a controlled manner and prevents any later command from running, for every input source.

## ADDED Requirements

### Requirement: HELP lists available commands
The `HELP` command SHALL print the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`, without changing any stored data.

#### Scenario: HELP in interactive mode
- **WHEN** `HELP` is entered on standard input
- **THEN** the output contains each of the five command syntaxes listed above
- **AND** the application keeps waiting for the next command

#### Scenario: HELP in file mode
- **WHEN** a command file contains `HELP`
- **THEN** the same help text as in interactive mode is printed and processing continues with the next line

### Requirement: EXIT terminates in a controlled manner
The `EXIT` command SHALL print a confirmation response starting with `OK`, stop command processing and terminate the application normally with exit status 0, keeping all previously persisted data.

#### Scenario: EXIT in interactive mode
- **WHEN** `EXIT` is entered on standard input while more input is still available
- **THEN** a response starting with `OK` is printed and the application terminates with exit status 0 without reading further input

#### Scenario: Data kept after EXIT
- **WHEN** a user is created, then `EXIT` is processed, and the application is started again
- **THEN** the created user is loaded

### Requirement: No command runs after EXIT
Once `EXIT` has been processed, no subsequent line from the same input source SHALL be processed, regardless of whether commands come from standard input or from a file.

#### Scenario: Command after EXIT in a file is not executed
- **WHEN** a command file contains `USER CREATE` for Ana, then `EXIT`, then `USER CREATE` for Pedro
- **THEN** Ana is created, `EXIT` is acknowledged, and Pedro is not created
- **AND** no response is printed for the line after `EXIT`

#### Scenario: Command after EXIT on standard input is not executed
- **WHEN** the same three lines are given on standard input
- **THEN** the output and resulting users are identical to the file case
