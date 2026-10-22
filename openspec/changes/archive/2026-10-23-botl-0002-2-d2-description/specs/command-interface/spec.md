# Spec Delta

## MODIFIED Requirements

### Requirement: Semicolon-separated command syntax
Each command SHALL be one line whose elements are separated by `;`. The first element is the command keyword (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`, `HELP`, `EXIT`). Leading and trailing whitespace around each element SHALL be ignored, and keywords SHALL be matched case-insensitively. Parsing SHALL use only basic `String` operations and `split`.

#### Scenario: Recognized command with arguments
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is interpreted as the `USER DELETE` command with arguments `ana@correo.com` and `Clave1!`

#### Scenario: Surrounding whitespace is ignored
- **WHEN** the line `  help  ` is processed
- **THEN** it is interpreted as the `HELP` command

#### Scenario: Delivery 2 keyword recognized
- **WHEN** the line `attraction status;A01` is processed
- **THEN** it is interpreted as the `ATTRACTION STATUS` command with argument `A01`

### Requirement: HELP command
The `HELP` command SHALL print the available commands and their syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<price>`, `GROUP CREATE;<identifier>;<name>`, `GROUP ADD;<identifier>;<email>`, `GROUP REMOVE;<identifier>;<email>`, `GROUP DELETE;<identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attraction>`, `HELP`, and `EXIT`.

#### Scenario: Help lists all commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, and `EXIT`

#### Scenario: Help lists Delivery 2 commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains the syntax of every `SYSTEM`, `GROUP`, `TICKET`, `GROUPTICKET`, and `ATTRACTION` command
