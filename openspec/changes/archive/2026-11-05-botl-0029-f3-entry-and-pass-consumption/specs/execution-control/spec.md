## MODIFIED Requirements

### Requirement: HELP lists available commands
The `HELP` command SHALL print the syntax of every command recognized by the application, including at least, in this order: `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>[;<type>]`, `ATTRACTION ENTER;<attraction>;<ticket>[;SPEED]`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`, without changing any stored data.

#### Scenario: HELP in interactive mode
- **WHEN** `HELP` is entered on standard input
- **THEN** the output contains each of the command syntaxes listed above, in that order
- **AND** the application keeps waiting for the next command

#### Scenario: HELP in file mode
- **WHEN** a command file contains `HELP`
- **THEN** the same help text as in interactive mode is printed and processing continues with the next line
