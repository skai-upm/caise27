# Spec Delta

## MODIFIED Requirements

### Requirement: Command syntax
Commands SHALL be lines whose elements are separated by `;`, interpreted with basic String operations including `split`. The recognised commands SHALL be `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`, `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, `INVOICE CREATE`, `HELP`, `HELP EXTENDED` and `EXIT`. Leading and trailing whitespace on a line SHALL be ignored.

#### Scenario: Unknown command
- **WHEN** the line `FOO;bar` is processed
- **THEN** an error is reported and the application continues accepting commands

#### Scenario: Wrong number of fields for a Delivery 2 command
- **WHEN** `TICKET CREATE;T001;ana@correo.com` is processed
- **THEN** an error is reported and no ticket is created

#### Scenario: Wrong number of fields for a Delivery 3 command
- **WHEN** `SPEEDPASS CREATE;SP01;T001` is processed
- **THEN** an error is reported and no pass is created

### Requirement: HELP command
`HELP` SHALL display the syntax of the commands of all three deliveries, at least: `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED`, `EXIT`.

#### Scenario: Help lists commands
- **WHEN** `HELP` is processed
- **THEN** the output contains each syntax entry listed above verbatim

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, for every command shown by `HELP`, its syntax together with a brief description of its purpose. The component that generates help SHALL receive the formatting behaviour by dependency injection, so new help variants can be added without changing command reading or execution.

#### Scenario: Extended help describes commands
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every syntax entry of `HELP` appears followed by a non-empty description

#### Scenario: Help behaviour is injected
- **WHEN** the help generator is constructed with a different formatting strategy
- **THEN** its output follows that strategy without changes to the command processor
