# execution-control Specification

## Purpose
Defines the general execution-control commands: `HELP`, which lists the available operations and their syntax, and `EXIT`, which ends the application in a controlled way so that no later command runs.

## Requirements

### Requirement: HELP command
The command `HELP` SHALL print the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `HELP` and `EXIT`, without changing any data.

#### Scenario: Help lists the commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `HELP` and `EXIT` and no user is modified

### Requirement: EXIT command
The command `EXIT` SHALL terminate the application in a controlled manner: it prints a farewell line, stops reading commands and ends with exit status 0, whether commands come from standard input or from a file.

#### Scenario: Exit in interactive mode
- **WHEN** `EXIT` is typed in interactive mode while standard input is still open
- **THEN** the application terminates with exit status 0 without waiting for more input

### Requirement: Nothing runs after EXIT
Once `EXIT` has been processed, no subsequent command SHALL be processed, in either input mode.

#### Scenario: Commands after EXIT in a file
- **WHEN** a file contains `USER CREATE;...` (valid user A), then `EXIT`, then `USER CREATE;...` (valid user B)
- **THEN** user A is created, user B is not created, and no response is printed for the third line
