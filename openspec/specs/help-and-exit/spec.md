# help-and-exit Specification

## Purpose

Provides the general control commands: `HELP` to list available commands and their syntax, and `EXIT` to terminate in a controlled way.

## Requirements

### Requirement: HELP command
`HELP` SHALL display the available commands and their basic syntax, including at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, the Delivery-2 SYSTEM, GROUP, TICKET, GROUPTICKET and ATTRACTION commands, `HELP` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is entered
- **THEN** each Delivery 1 and Delivery 2 command and its syntax is displayed

#### Scenario: Delivery-2 commands listed
- **WHEN** `HELP` is entered
- **THEN** the output includes `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>` and `ATTRACTION STATUS;<attractionIdentifier>`

### Requirement: EXIT command
`EXIT` SHALL terminate command processing in a controlled manner in both interactive and file mode; in file mode no line after `EXIT` SHALL be processed.

#### Scenario: Interactive exit
- **WHEN** `EXIT` is entered interactively
- **THEN** the application stops reading input and terminates normally

#### Scenario: Commands after EXIT are not executed
- **WHEN** a file contains `USER CREATE;...`, then `EXIT`, then another `USER CREATE;...`
- **THEN** only the first user is created
