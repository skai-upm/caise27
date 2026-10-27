# Spec Delta

## Purpose

Defines how the application receives textual commands from standard input or from a command file, processes them through one shared command path, and provides the general HELP and EXIT commands.

## ADDED Requirements

### Requirement: Command input sources
The application SHALL read commands one per line from standard input when started without arguments, and from the text file whose path is the first argument when one is given. Commands SHALL be processed in the order they are read, and the observable behaviour of each command SHALL be the same for both sources.

#### Scenario: Commands from standard input
- **WHEN** the application starts without arguments and receives `HELP` on standard input
- **THEN** it prints the help text and waits for the next command

#### Scenario: Commands from a file
- **WHEN** the application starts with a file argument whose first line is `HELP`
- **THEN** it prints the same help text as in interactive mode

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least: `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.

#### Scenario: Help lists the required commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner, and no command received after `EXIT` SHALL be executed, regardless of the input source.

#### Scenario: Exit in interactive mode
- **WHEN** the user enters `EXIT` on standard input
- **THEN** the application stops reading commands and ends normally

#### Scenario: Exit stops file processing
- **WHEN** a command file contains a command, then `EXIT`, then another command
- **THEN** the command before `EXIT` is processed and the line after `EXIT` is not processed

#### Scenario: End of input without EXIT
- **WHEN** the input ends without an `EXIT` command
- **THEN** the application ends normally after processing all commands

### Requirement: Unrecognised commands
A line that does not match an available command SHALL be rejected with an error message, without terminating the application.

#### Scenario: Unknown command is rejected
- **WHEN** the user enters `FOO`
- **THEN** an error message indicating that the command is not recognised is printed and the next command is still processed
