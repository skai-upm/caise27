# execution-control Specification

## Purpose
Defines the general execution-control commands: `HELP`, which lists the available operations and their syntax, and `EXIT`, which ends the application in a controlled way so that no later command runs.

## Requirements

### Requirement: HELP command
The command `HELP` SHALL print the available commands and their basic syntax, including at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP` and `EXIT`, without changing any data.

#### Scenario: Help lists the commands
- **WHEN** `HELP` is processed
- **THEN** the output contains each of the listed syntax strings exactly and no data is modified

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
