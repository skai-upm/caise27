# help-and-exit Specification

## Purpose

Provides the general control commands: `HELP` to show the available operations and their syntax, and `EXIT` to terminate processing in a controlled way in both input modes.

## Requirements

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax for Delivery 1 and Delivery 2, including at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, the GROUP, TICKET, GROUPTICKET and ATTRACTION commands with the syntax of source §15, `HELP` and `EXIT`. (F1-C08, F2-C10)

#### Scenario: Help in interactive mode
- **WHEN** `HELP` is entered
- **THEN** the user commands and control commands above are listed with their syntax

#### Scenario: Delivery 2 commands listed
- **WHEN** `HELP` is entered
- **THEN** the output contains `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>` and `ATTRACTION STATUS;<attractionIdentifier>`

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner in both interactive and file mode; in file mode no line after `EXIT` SHALL be processed. (F1-C08)

#### Scenario: Command after EXIT in a file
- **WHEN** a file contains `USER CREATE;...`, `EXIT` and another `USER CREATE;...`
- **THEN** only the first user is created and the second command is not executed

#### Scenario: EXIT interactively
- **WHEN** `EXIT` is entered on standard input
- **THEN** the application stops reading input and terminates normally
