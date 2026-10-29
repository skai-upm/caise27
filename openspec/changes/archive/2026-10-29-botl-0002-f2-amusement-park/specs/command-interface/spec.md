# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
`HELP` SHALL display the available commands and their basic syntax, covering at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** each of the twenty command syntaxes is printed

## ADDED Requirements

### Requirement: Command-line argument forms
The application SHALL accept exactly these launch forms: no arguments (interactive), `<file>` (file mode), `--fly` (interactive, temporary persistence), and `--fly <file>` (file mode, temporary persistence). Without `--fly`, argument handling SHALL be unchanged from Delivery 1.

#### Scenario: Fly followed by file
- **WHEN** the JAR is run with `--fly input_fase2.txt`
- **THEN** the commands in `input_fase2.txt` are processed using temporary persistence
