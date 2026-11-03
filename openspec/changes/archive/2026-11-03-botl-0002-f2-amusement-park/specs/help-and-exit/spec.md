# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
`HELP` SHALL display the available commands and their basic syntax, including at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, the Delivery-2 SYSTEM, GROUP, TICKET, GROUPTICKET and ATTRACTION commands, `HELP` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is entered
- **THEN** each Delivery 1 and Delivery 2 command and its syntax is displayed

#### Scenario: Delivery-2 commands listed
- **WHEN** `HELP` is entered
- **THEN** the output includes `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>` and `ATTRACTION STATUS;<attractionIdentifier>`
