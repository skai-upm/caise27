# help-and-exit Specification

## Purpose

Provides the general `HELP` command listing available command syntax and the `EXIT` command terminating execution in a controlled way in both input modes (source F1-C08, sections 11 and 12).

## Requirements

### Requirement: HELP lists available commands
The `HELP` command SHALL display the available commands and their basic syntax, including at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, the `GROUP`, `TICKET`, `GROUPTICKET`, `ATTRACTION`, `SPEEDPASS`, `SPECIALPASS` and `INVOICE` commands, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: HELP in interactive mode
- **WHEN** `HELP` is entered in an interactive session
- **THEN** the syntax of all user commands and of `HELP` and `EXIT` is displayed and the session continues

#### Scenario: HELP lists Phase 2 commands
- **WHEN** `HELP` is processed
- **THEN** the output contains `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>` and `ATTRACTION STATUS;<attractionIdentifier>`

#### Scenario: HELP lists Delivery 3 commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>` and `HELP EXTENDED`

### Requirement: EXIT terminates processing
The `EXIT` command SHALL terminate the application in a controlled manner in both interactive and file mode; in file mode, no line after `EXIT` SHALL be processed.

#### Scenario: EXIT in interactive mode
- **WHEN** `EXIT` is entered in an interactive session
- **THEN** the application stops reading input and terminates normally

#### Scenario: Commands after EXIT in a file are ignored
- **WHEN** a file containing `USER CREATE` for Ana, then `EXIT`, then `USER CREATE` for Pedro is processed
- **THEN** Ana is created and Pedro is not
