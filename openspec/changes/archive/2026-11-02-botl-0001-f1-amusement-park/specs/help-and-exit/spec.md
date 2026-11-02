# Spec Delta

## Purpose

Defines the general `HELP` and `EXIT` commands that list available operations and terminate processing in a controlled way in both input modes (F1-C08).

## ADDED Requirements

### Requirement: HELP command
`HELP` SHALL display the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.

#### Scenario: Help lists all commands
- **WHEN** `HELP` is processed in interactive or file mode
- **THEN** the output lists the syntax of USER CREATE, USER DELETE, USER UPDATE, HELP and EXIT

### Requirement: EXIT command
`EXIT` SHALL terminate the application in a controlled manner. Once `EXIT` is encountered, no subsequent command SHALL be processed, in either interactive or file mode.

#### Scenario: Commands after EXIT in a file are not executed
- **WHEN** a file contains `USER CREATE` for user A, then `EXIT`, then `USER CREATE` for user B
- **THEN** user A is created, user B is not created, and processing stops

#### Scenario: Interactive EXIT
- **WHEN** `EXIT` is entered interactively
- **THEN** the application stops reading input and terminates normally
