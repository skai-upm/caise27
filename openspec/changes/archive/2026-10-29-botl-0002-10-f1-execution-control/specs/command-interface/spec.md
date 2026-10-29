# Spec Delta

## Purpose

Defines how the Amusement Park application receives and processes text commands from its input sources, and the general HELP and EXIT commands that control execution.

## ADDED Requirements

### Requirement: Source-independent command processing
The application SHALL process commands one per line, in order, through a single command-processing mechanism regardless of whether the lines come from standard input or from a command file, so that the observable result of a command is the same for both sources.

#### Scenario: Same output from both sources
- **WHEN** the same sequence of command lines is processed once from standard input and once from a file
- **THEN** the produced output is identical

#### Scenario: Interactive mode without arguments
- **WHEN** the application is started without arguments
- **THEN** it reads commands from standard input and, after processing each command, waits for the next one

#### Scenario: File given as first argument
- **WHEN** the application is started with a file path as its first argument
- **THEN** it processes the commands contained in that file

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.

#### Scenario: HELP lists the minimum command set
- **WHEN** the line `HELP` is processed
- **THEN** the output contains each of the syntaxes `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`

#### Scenario: HELP does not stop processing
- **WHEN** `HELP` is followed by another command line
- **THEN** the following command is also processed

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner, and once EXIT is processed no subsequent line of the input SHALL be processed.

#### Scenario: EXIT in file mode stops later lines
- **WHEN** a file containing `HELP`, then `EXIT`, then `HELP` is processed
- **THEN** the help text is produced exactly once and the line after `EXIT` is not executed

#### Scenario: EXIT from standard input
- **WHEN** `EXIT` is read from standard input
- **THEN** the application stops reading further input and terminates normally without an error

### Requirement: Unrecognised commands are rejected
A non-blank line that does not match an available command SHALL be rejected with an error message and SHALL NOT stop the processing of later lines.

#### Scenario: Unknown command followed by HELP
- **WHEN** the lines `FOO` and `HELP` are processed
- **THEN** an error message is shown for `FOO` and the help text is shown for `HELP`
