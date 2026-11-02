# help-and-exit Specification

## Purpose

Defines the general `HELP` and `EXIT` commands that list available operations and terminate processing in a controlled way in both input modes (F1-C08).

## Requirements

### Requirement: HELP command
`HELP` SHALL display the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`. From Delivery 2 it SHALL also list `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>` and `ATTRACTION STATUS;<attractionIdentifier>`. From Delivery 3 it SHALL also list `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>` and `HELP EXTENDED`.

#### Scenario: Help lists all commands
- **WHEN** `HELP` is processed in interactive or file mode
- **THEN** the output lists the syntax of USER CREATE, USER DELETE, USER UPDATE, HELP and EXIT

#### Scenario: Help lists Delivery 2 commands
- **WHEN** `HELP` is processed
- **THEN** the output also lists the syntax of every SYSTEM, GROUP, TICKET, GROUPTICKET and ATTRACTION command

#### Scenario: Help lists Delivery 3 commands
- **WHEN** `HELP` is processed
- **THEN** the output also lists the syntax of the typed ATTRACTION CREATE, the SPEED entry, the SPEEDPASS, SPECIALPASS and INVOICE commands and HELP EXTENDED

### Requirement: EXIT command
`EXIT` SHALL terminate the application in a controlled manner. Once `EXIT` is encountered, no subsequent command SHALL be processed, in either interactive or file mode.

#### Scenario: Commands after EXIT in a file are not executed
- **WHEN** a file contains `USER CREATE` for user A, then `EXIT`, then `USER CREATE` for user B
- **THEN** user A is created, user B is not created, and processing stops

#### Scenario: Interactive EXIT
- **WHEN** `EXIT` is entered interactively
- **THEN** the application stops reading input and terminates normally
