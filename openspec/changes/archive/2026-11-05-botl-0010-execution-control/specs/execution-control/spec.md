# Spec Delta

## Purpose

Defines the general commands that control the application's execution: `HELP`, which lists the available commands and their syntax, and `EXIT`, which ends the application in a controlled way.

## ADDED Requirements

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`, and SHALL NOT change any data.

#### Scenario: Help in interactive mode
- **WHEN** `HELP` is entered on standard input
- **THEN** the syntax of USER CREATE, USER DELETE, USER UPDATE, HELP and EXIT is displayed and the application keeps waiting for commands

#### Scenario: Help in file mode
- **GIVEN** a command file containing `HELP`
- **WHEN** the file is processed
- **THEN** the same help text is displayed

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner with exit status 0. Once `EXIT` is processed, no subsequent command MUST be executed, whether commands come from standard input or from a file.

#### Scenario: Exit in interactive mode
- **WHEN** `EXIT` is entered on standard input
- **THEN** the application terminates with exit status 0 without waiting for more input

#### Scenario: No command after EXIT in file mode
- **GIVEN** a command file with a `USER CREATE` for Ana, then `EXIT`, then a `USER CREATE` for Pedro
- **WHEN** the file is processed
- **THEN** Ana is created, the application terminates, and Pedro is not created

#### Scenario: General commands take no arguments
- **WHEN** `EXIT;now` or `HELP;x` is processed
- **THEN** an error message starting with `ERROR` is printed and the application continues
