# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least the Delivery 2 minimum command set exactly as spelled in the scenario below, and SHALL NOT stop processing.

#### Scenario: HELP lists the minimum command set
- **WHEN** the line `HELP` is processed
- **THEN** the output contains each of `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP` and `EXIT`

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

## REMOVED Requirements

### Requirement: HELP lists group ticket commands
**Reason**: Consolidated into the Delivery 2 minimum command set of "HELP command".
**Migration**: The GROUPTICKET syntaxes are asserted by the "HELP command" scenario.

### Requirement: HELP lists attraction commands
**Reason**: Consolidated into the Delivery 2 minimum command set of "HELP command".
**Migration**: The ATTRACTION CREATE and ATTRACTION LIST syntaxes are asserted by the "HELP command" scenario.

### Requirement: HELP lists attraction entry
**Reason**: Consolidated into the Delivery 2 minimum command set of "HELP command".
**Migration**: The ATTRACTION ENTER syntax is asserted by the "HELP command" scenario.

### Requirement: HELP lists attraction exit and status
**Reason**: Consolidated into the Delivery 2 minimum command set of "HELP command", which uses the Delivery 2 spelling `ATTRACTION STATUS;<attractionIdentifier>`.
**Migration**: The ATTRACTION EXIT and ATTRACTION STATUS syntaxes are asserted by the "HELP command" scenario.
