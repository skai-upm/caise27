## MODIFIED Requirements

### Requirement: HELP command
The system SHALL accept the command `HELP` and display the available commands and their basic syntax, including at least the lines `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<Identifier>;<Name>;<Capacity>`, `ATTRACTION LIST;<AttractionIdentifier>`, `HELP` and `EXIT`.

#### Scenario: Help lists mandatory syntax
- **WHEN** the command `HELP` is processed
- **THEN** the output contains each of the lines `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<Identifier>;<Name>;<Capacity>`, `ATTRACTION LIST;<AttractionIdentifier>`, `HELP` and `EXIT`

#### Scenario: Help does not end the session
- **WHEN** the commands `HELP` and then `HELP` are processed from the same source
- **THEN** the help listing is displayed twice
