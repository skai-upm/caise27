# execution-control Specification

## Purpose

Defines the general commands that control execution: `HELP`, which lists the available operations and their syntax, and `EXIT`, which ends the application in a controlled way.

## Requirements

### Requirement: HELP command
The command `HELP` SHALL display the available commands of Phase 1 and Phase 2 and their basic syntax, including at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP` and `EXIT`.

#### Scenario: Help lists the commands
- **WHEN** `HELP` is processed
- **THEN** the output contains every syntax line listed above

#### Scenario: Help in file mode
- **WHEN** a command file contains the line `HELP`
- **THEN** the same help text as in interactive mode is written to standard output

### Requirement: EXIT command
The command `EXIT` SHALL terminate the application in a controlled manner with exit status 0, and no subsequent command SHALL be executed.

#### Scenario: Exit stops file processing
- **WHEN** a command file contains `USER CREATE` for Ana, then `EXIT`, then `USER CREATE` for Pedro
- **THEN** only Ana is created, the second USER CREATE is not executed and the application ends with status 0

#### Scenario: Exit in interactive mode
- **WHEN** `EXIT` is typed in interactive mode while more input follows
- **THEN** the application terminates without reading or executing the following lines

### Requirement: Help lists invoice commands
HELP SHALL also list `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`.

#### Scenario: Invoice syntax in help
- **WHEN** `HELP` is processed
- **THEN** both invoice syntax lines are shown
