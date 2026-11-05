## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least, in this order, `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP` and `EXIT`, and SHALL NOT change any data.

#### Scenario: Help in interactive mode
- **WHEN** `HELP` is entered on standard input
- **THEN** the syntax of USER CREATE, USER DELETE, USER UPDATE, HELP and EXIT is displayed and the application keeps waiting for commands

#### Scenario: Help in file mode
- **GIVEN** a command file containing `HELP`
- **WHEN** the file is processed
- **THEN** the same help text is displayed

#### Scenario: Complete Delivery 2 syntax listed in order
- **WHEN** `HELP` is processed
- **THEN** each of the listed syntax lines appears exactly as written and in the listed order
