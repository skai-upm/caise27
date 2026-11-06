# execution-control Specification

## Purpose

Provides the general `HELP` and `EXIT` commands that show the available operations and terminate the Amusement Park application in a controlled way, identically for interactive and file input.

## Requirements

### Requirement: HELP lists available commands
The `HELP` command SHALL print the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `GROUP CREATE;<id>;<name>`, `GROUP ADD;<id>;<email>`, `GROUP REMOVE;<id>;<email>`, `GROUP DELETE;<id>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<price>`, `HELP` and `EXIT`, and MUST NOT change any stored data.

#### Scenario: Help in interactive mode
- **WHEN** `HELP` is entered on standard input
- **THEN** the output contains each of the command syntaxes listed above and processing continues with the next line

#### Scenario: Help in file mode
- **GIVEN** a command file whose only line is `HELP`
- **WHEN** the application processes it
- **THEN** the same help text as in interactive mode is printed

### Requirement: EXIT terminates in a controlled manner
The `EXIT` command SHALL print a closing message and end the application normally; once `EXIT` is processed no subsequent line, from standard input or from a command file, MUST be processed.

#### Scenario: Commands after EXIT in a file are not executed
- **GIVEN** a command file with `USER CREATE` for DNI `12345678Z`, then `EXIT`, then `USER CREATE` for DNI `87654321X`
- **WHEN** the application processes it
- **THEN** only the user `12345678Z` is created, the closing message is printed, and nothing is printed for the third line

#### Scenario: EXIT in interactive mode
- **GIVEN** standard input with `EXIT` followed by `HELP`
- **WHEN** the application runs
- **THEN** the closing message is printed, the help text is not printed, and the process ends normally

### Requirement: HELP and EXIT take no fields
`HELP` and `EXIT` SHALL be recognized only without additional `;`-separated fields; a line such as `EXIT;now` MUST be rejected with an error message and MUST NOT terminate the application.

#### Scenario: EXIT with fields is rejected
- **WHEN** `EXIT;now` is processed followed by `HELP`
- **THEN** an error message is printed for the first line and the help text is printed for the second
