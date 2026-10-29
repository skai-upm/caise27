# Spec Delta

## MODIFIED Requirements

### Requirement: Command syntax
Commands SHALL be single lines whose elements are separated by `;`, interpreted with basic String operations including `split`. The first element identifies the command; the recognised commands are `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`, `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, `INVOICE CREATE`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Recognised command is dispatched
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user deletion operation is executed with email `ana@correo.com` and password `Clave1!`

#### Scenario: Unknown command
- **WHEN** the line `USER LIST` is processed
- **THEN** an error message is shown and the application keeps running

#### Scenario: Wrong number of arguments
- **WHEN** the line `USER DELETE;ana@correo.com` is processed
- **THEN** an error message is shown, no operation is executed and the application keeps running

#### Scenario: Delivery 2 command is dispatched
- **WHEN** the line `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** the group creation operation is executed

#### Scenario: Delivery 3 command is dispatched
- **WHEN** the line `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** the SpeedPass purchase operation is executed

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Help lists commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of every Delivery 1, 2 and 3 command listed above, including HELP, HELP EXTENDED and EXIT

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, for every command shown by HELP, its syntax together with a brief explanation of its purpose. The help generator MUST receive the output-building behaviour by dependency injection, independent of command reading and execution.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command syntax appears with a description of its purpose

#### Scenario: Injected format
- **WHEN** a help generator is created with a different injected format
- **THEN** it produces output in that format without changes to command processing
