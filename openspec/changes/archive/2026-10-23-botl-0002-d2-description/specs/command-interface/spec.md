# Spec Delta

## MODIFIED Requirements

### Requirement: File mode
When started with a file path as its first argument, or with `--fly` followed by a file path, the application SHALL process the commands in that file in order, one per line, ignoring blank lines, using exactly the same command processing as interactive mode.

#### Scenario: Commands from a file
- **WHEN** the JAR is run with a readable file containing `USER CREATE` and `USER UPDATE` commands
- **THEN** each command is executed in file order and produces the same output it would in interactive mode

#### Scenario: Blank lines
- **WHEN** the command file contains blank or whitespace-only lines
- **THEN** those lines are skipped without producing an error

#### Scenario: Missing or unreadable file
- **WHEN** the JAR is run with a path that does not exist or cannot be read
- **THEN** the application prints an error message identifying the file and terminates without a stack trace

#### Scenario: File after --fly
- **WHEN** the JAR is run as `--fly input.txt`
- **THEN** the commands in `input.txt` are processed exactly as in normal file mode

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, covering at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP` and `EXIT`.

#### Scenario: Show help
- **WHEN** `HELP` is processed
- **THEN** the output lists the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`

#### Scenario: Delivery 2 commands in help
- **WHEN** `HELP` is processed
- **THEN** the output also lists the syntax of every `SYSTEM`, `GROUP`, `TICKET`, `GROUPTICKET` and `ATTRACTION` command
