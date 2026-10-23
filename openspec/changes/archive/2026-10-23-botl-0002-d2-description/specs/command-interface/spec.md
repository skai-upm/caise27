# Spec Delta

## MODIFIED Requirements

### Requirement: Supported commands
The application SHALL recognise the commands `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`, `HELP` and `EXIT`.

#### Scenario: Unknown command
- **WHEN** the line `USER LIST` is processed
- **THEN** an error message stating that the command is unknown and suggesting `HELP` is printed and the application keeps running

#### Scenario: Delivery 2 command recognised
- **WHEN** the line `SYSTEM PRICE;45.50` is processed
- **THEN** it is dispatched to the price-setting operation

### Requirement: HELP command
The `HELP` command SHALL print the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>` (listing the modifiable fields), `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<price>`, `GROUP CREATE;<identifier>;<name>`, `GROUP ADD;<identifier>;<email>`, `GROUP REMOVE;<identifier>;<email>`, `GROUP DELETE;<identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attraction>`, `HELP` and `EXIT`.

#### Scenario: Show help
- **WHEN** `HELP` is processed
- **THEN** the syntax of all twenty commands is printed
