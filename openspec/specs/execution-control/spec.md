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

### Requirement: HELP lists invoice commands
`HELP` SHALL also list `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`.

#### Scenario: Help shows invoices
- **WHEN** `HELP` is processed
- **THEN** both invoice command forms are shown

### Requirement: HELP EXTENDED command
The command `HELP EXTENDED` SHALL print, for every command recognized by the application, its syntax together with a brief explanation of its purpose, without changing any data. `HELP` SHALL keep printing the syntax list, which also includes `HELP EXTENDED`.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command syntax shown by `HELP` appears with an explanation next to it

#### Scenario: Basic help unchanged
- **WHEN** `HELP` is processed
- **THEN** it shows the command syntax list without explanations

### Requirement: Injected help generation
The component that generates help text SHALL receive, through dependency injection, the behavior used to build the output; supporting a new kind of help SHALL only require providing a new behavior and wiring it, without changing the logic that reads and executes commands.

#### Scenario: Alternative help behavior
- **WHEN** the help generator is constructed with a different output behavior
- **THEN** it produces that output for the same command catalog, and the command processor is unchanged

### Requirement: HELP lists pass commands
`HELP` and `HELP EXTENDED` SHALL also list `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>` and `SPECIALPASS DELETE;<specialPassIdentifier>`.

#### Scenario: Help shows passes
- **WHEN** `HELP` is processed
- **THEN** the four pass command syntaxes are shown

### Requirement: HELP lists SpeedPass entry
`HELP` and `HELP EXTENDED` SHALL also list `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`.

#### Scenario: Help shows the SPEED variant
- **WHEN** `HELP` is processed
- **THEN** `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` is shown
