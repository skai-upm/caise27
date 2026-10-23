# Spec Delta

## MODIFIED Requirements

### Requirement: Supported commands
The application SHALL recognise the commands `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`, `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, `INVOICE CREATE`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Unknown command
- **WHEN** the line `USER LIST` is processed
- **THEN** an error message stating that the command is unknown and suggesting `HELP` is printed and the application keeps running

#### Scenario: Delivery 2 command recognised
- **WHEN** the line `SYSTEM PRICE;45.50` is processed
- **THEN** it is dispatched to the price-setting operation

#### Scenario: Delivery 3 command recognised
- **WHEN** the line `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** it is dispatched to the SpeedPass purchase operation

### Requirement: HELP command
The `HELP` command SHALL print the syntax of every supported command, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>` (listing the modifiable fields), `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<price>`, `GROUP CREATE;<identifier>;<name>`, `GROUP ADD;<identifier>;<email>`, `GROUP REMOVE;<identifier>;<email>`, `GROUP DELETE;<identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attraction>`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Show help
- **WHEN** `HELP` is processed
- **THEN** the syntax of all supported commands is printed

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL print every syntax shown by `HELP` together with a short description of the purpose of each command.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each command syntax is printed followed by a description, e.g. that `SPEEDPASS CREATE` buys 1 or 10 SpeedPasses for an individual ticket
