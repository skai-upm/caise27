# help-and-exit Specification

## Purpose

Provides the general `HELP` command that lists available commands and their syntax, and the `EXIT` command that terminates processing in a controlled manner in both input modes.

## Requirements

### Requirement: HELP command
`HELP` SHALL display the available commands and their basic syntax, including at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, every `GROUP`, `TICKET`, `GROUPTICKET` and `ATTRACTION` command of Delivery 2, `HELP` and `EXIT`.

#### Scenario: Interactive help
- **WHEN** `HELP` is entered
- **THEN** the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT` is displayed and the application keeps waiting for commands

#### Scenario: Delivery 2 commands listed
- **WHEN** `HELP` is entered
- **THEN** it also lists `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>` and `ATTRACTION STATUS;<attractionIdentifier>`

### Requirement: EXIT command
`EXIT` SHALL terminate the application in a controlled manner in interactive and file mode; in file mode no line after `EXIT` SHALL be processed.

#### Scenario: Interactive exit
- **WHEN** `EXIT` is entered interactively
- **THEN** the application stops reading commands and terminates normally

#### Scenario: Commands after EXIT in a file
- **WHEN** a file containing `USER CREATE;...`, then `EXIT`, then another `USER CREATE;...` is processed
- **THEN** the first user is created and the second `USER CREATE` is not executed
