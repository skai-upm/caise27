## MODIFIED Requirements

### Requirement: HELP lists available commands
The `HELP` command SHALL print the syntax of the commands of the three deliveries, each on its own line, including at least, in this order: `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`, without changing any stored data. Delivery 1 and 2 syntaxes SHALL keep appearing and working; the Delivery 3 forms of `ATTRACTION CREATE` (with type) and `ATTRACTION ENTER` (with `SPEED`) SHALL be listed as separate syntaxes.

#### Scenario: HELP in interactive mode
- **WHEN** `HELP` is entered on standard input
- **THEN** the output contains each of the command syntaxes listed above, in that order
- **AND** the application keeps waiting for the next command

#### Scenario: HELP in file mode
- **WHEN** a command file contains `HELP`
- **THEN** the same help text as in interactive mode is printed and processing continues with the next line

#### Scenario: Delivery 3 syntax listed literally
- **WHEN** `HELP` is processed
- **THEN** the output contains the lines `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` and `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` in addition to the Delivery 2 forms `ATTRACTION CREATE;<identifier>;<name>;<capacity>` and `ATTRACTION ENTER;<attraction>;<ticket>`
