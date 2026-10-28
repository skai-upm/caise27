## MODIFIED Requirements

### Requirement: HELP command
The system SHALL accept the command `HELP` and display the available commands and their basic syntax, including at least the lines `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Help lists mandatory syntax
- **WHEN** the command `HELP` is processed
- **THEN** the output contains each of the lines `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`

#### Scenario: Help does not end the session
- **WHEN** the commands `HELP` and then `HELP` are processed from the same source
- **THEN** the help listing is displayed twice

## ADDED Requirements

### Requirement: HELP EXTENDED command
The system SHALL accept `HELP EXTENDED` and display, for every command listed by `HELP`, its syntax together with a brief explanation of its purpose.

#### Scenario: Extended help explains each command
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every syntax line shown by `HELP` appears, each followed by a non-empty explanation of the command's purpose

#### Scenario: Plain help unchanged
- **WHEN** `HELP` is processed after `HELP EXTENDED`
- **THEN** it shows only the syntax lines, without explanations

### Requirement: Injected help generation behaviour
The component that generates help text SHALL receive the behaviour used to build the output through dependency injection, so that new help variants can be added without changing command reading and execution.

#### Scenario: New help style without changing command processing
- **WHEN** the help generator is constructed with a different formatting behaviour
- **THEN** it produces output in that style from the same command catalogue, with no change to the command processor
