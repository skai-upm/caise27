# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `HELP` and `EXIT`.

#### Scenario: HELP lists the minimum command set
- **WHEN** the line `HELP` is processed
- **THEN** the output contains each of the syntaxes `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`

#### Scenario: HELP lists the SYSTEM commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`

#### Scenario: HELP lists the GROUP commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>` and `GROUP DELETE;<Identifier>`

#### Scenario: HELP lists the TICKET commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `TICKET CREATE;<identifier>;<email>;<date>` and `TICKET DELETE;<identifier>`

#### Scenario: HELP does not stop processing
- **WHEN** `HELP` is followed by another command line
- **THEN** the following command is also processed
