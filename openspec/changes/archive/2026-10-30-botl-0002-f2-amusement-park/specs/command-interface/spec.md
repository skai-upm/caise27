# Spec Delta

## MODIFIED Requirements

### Requirement: Command syntax
Commands SHALL consist of elements separated by `;` and be interpreted using basic String operations including split, without external libraries. The recognised commands are `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`, `HELP` and `EXIT`.

#### Scenario: Unknown command
- **WHEN** a line such as `FOO BAR` or `USER LIST` is processed
- **THEN** an `ERROR:` message is printed and the application keeps running

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, at least: `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, the four GROUP, two TICKET, two GROUPTICKET and five ATTRACTION syntaxes, `HELP` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** every syntax listed in the Delivery 2 HELP section, including `GROUPTICKET CREATE;<identifier>;<group>;<date>` and `ATTRACTION STATUS;<attractionIdentifier>`, is printed
