# Spec Delta

## MODIFIED Requirements

### Requirement: HELP lists available commands and syntax
The system SHALL respond to the command `HELP` by displaying the available commands with their basic syntax, including at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP` and `EXIT`.

#### Scenario: HELP in interactive mode
- **GIVEN** the application reading commands from standard input
- **WHEN** the user enters `HELP`
- **THEN** the output contains the syntax of USER CREATE, USER DELETE, USER UPDATE, HELP and EXIT
- **AND** the application keeps waiting for the next command

#### Scenario: HELP from a command file
- **GIVEN** a command file whose first line is `HELP`
- **WHEN** the file is processed
- **THEN** the output is the same help text as in interactive mode
- **AND** processing continues with the next line

#### Scenario: HELP shows the complete Delivery 2 syntax
- **WHEN** `HELP` is processed
- **THEN** every syntax line listed in this requirement appears verbatim in the output
