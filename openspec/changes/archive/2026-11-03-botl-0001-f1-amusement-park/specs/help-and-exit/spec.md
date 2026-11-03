# Spec Delta

## Purpose

Provides the general `HELP` command listing available command syntax and the `EXIT` command terminating execution in a controlled way in both input modes (source F1-C08, sections 11 and 12).

## ADDED Requirements

### Requirement: HELP lists available commands
The `HELP` command SHALL display the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.

#### Scenario: HELP in interactive mode
- **WHEN** `HELP` is entered in an interactive session
- **THEN** the syntax of all user commands and of `HELP` and `EXIT` is displayed and the session continues

### Requirement: EXIT terminates processing
The `EXIT` command SHALL terminate the application in a controlled manner in both interactive and file mode; in file mode, no line after `EXIT` SHALL be processed.

#### Scenario: EXIT in interactive mode
- **WHEN** `EXIT` is entered in an interactive session
- **THEN** the application stops reading input and terminates normally

#### Scenario: Commands after EXIT in a file are ignored
- **WHEN** a file containing `USER CREATE` for Ana, then `EXIT`, then `USER CREATE` for Pedro is processed
- **THEN** Ana is created and Pedro is not
