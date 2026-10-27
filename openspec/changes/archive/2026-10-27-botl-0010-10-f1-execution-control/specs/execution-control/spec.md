# Spec Delta

## Purpose

Defines the general commands that control execution: `HELP`, which lists the available operations and their syntax, and `EXIT`, which ends the application in a controlled way.

## ADDED Requirements

### Requirement: HELP command
The command `HELP` SHALL display the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.

#### Scenario: Help lists the commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax lines for USER CREATE, USER DELETE, USER UPDATE, HELP and EXIT

#### Scenario: Help in file mode
- **WHEN** a command file contains the line `HELP`
- **THEN** the same help text as in interactive mode is written to standard output

### Requirement: EXIT command
The command `EXIT` SHALL terminate the application in a controlled manner with exit status 0, and no subsequent command SHALL be executed.

#### Scenario: Exit stops file processing
- **WHEN** a command file contains `USER CREATE` for Ana, then `EXIT`, then `USER CREATE` for Pedro
- **THEN** only Ana is created, the second USER CREATE is not executed and the application ends with status 0

#### Scenario: Exit in interactive mode
- **WHEN** `EXIT` is typed in interactive mode while more input follows
- **THEN** the application terminates without reading or executing the following lines
