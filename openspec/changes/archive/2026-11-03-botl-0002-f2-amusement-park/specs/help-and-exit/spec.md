# Spec Delta

## MODIFIED Requirements

### Requirement: HELP lists available commands
The `HELP` command SHALL display the available commands and their basic syntax, including at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, the `GROUP`, `TICKET`, `GROUPTICKET` and `ATTRACTION` commands, `HELP` and `EXIT`.

#### Scenario: HELP in interactive mode
- **WHEN** `HELP` is entered in an interactive session
- **THEN** the syntax of all user commands and of `HELP` and `EXIT` is displayed and the session continues

#### Scenario: HELP lists Phase 2 commands
- **WHEN** `HELP` is processed
- **THEN** the output contains `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>` and `ATTRACTION STATUS;<attractionIdentifier>`
