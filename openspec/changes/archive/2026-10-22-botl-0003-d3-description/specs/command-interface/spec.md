# Spec Delta

## MODIFIED Requirements

### Requirement: Command syntax
Every command SHALL be a single line whose elements are separated by `;`. The first element SHALL be the command name (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`, `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, `INVOICE CREATE`, `HELP`, `HELP EXTENDED`, `EXIT`), followed by its arguments. Interpretation SHALL use basic String operations, without regular expressions or external libraries.

#### Scenario: Recognized command
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is interpreted as the `USER DELETE` command with arguments `ana@correo.com` and `Clave1!`

#### Scenario: Park command
- **WHEN** the line `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** it is interpreted as the `GROUP ADD` command with arguments `G01` and `ana@correo.com`

#### Scenario: Optional argument
- **WHEN** the lines `ATTRACTION ENTER;A01;T001` and `ATTRACTION ENTER;A01;T001;SPEED` are processed
- **THEN** both are interpreted as `ATTRACTION ENTER`, the second with the `SPEED` option

### Requirement: HELP command
The `HELP` command SHALL display every available command and its syntax, covering at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Show help
- **WHEN** `HELP` is processed
- **THEN** each available command and its syntax is printed

#### Scenario: Help lists park commands
- **WHEN** `HELP` is processed
- **THEN** the output includes `GROUPTICKET CREATE;<identifier>;<group>;<date>` and `ATTRACTION STATUS;<attractionIdentifier>`

#### Scenario: Help lists Delivery 3 commands
- **WHEN** `HELP` is processed
- **THEN** the output includes `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` and `INVOICE CREATE;GROUP;<groupIdentifier>`

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, for every command shown by `HELP`, its syntax together with a short description of its purpose.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command syntax is printed followed by a description, e.g. what `SPEEDPASS CREATE` buys
