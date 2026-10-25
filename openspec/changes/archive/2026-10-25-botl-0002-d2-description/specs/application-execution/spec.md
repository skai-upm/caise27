# Spec Delta

## MODIFIED Requirements

### Requirement: Command syntax
The application SHALL interpret each input line as a command whose elements are separated by the `;` character. The first element SHALL identify the command (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`, `HELP`, `EXIT`). Command keywords SHALL be recognised regardless of letter case and surrounding whitespace. Parsing SHALL use basic String operations and `split`, without regular expressions or external libraries.

#### Scenario: Recognised command
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is handled as the `USER DELETE` command with arguments `ana@correo.com` and `Clave1!`

#### Scenario: Unknown command
- **WHEN** the line `FOO BAR;1;2` is processed
- **THEN** the application reports an error message stating the command is unknown and continues running

#### Scenario: Wrong number of arguments
- **WHEN** the line `USER DELETE;ana@correo.com` is processed
- **THEN** the application reports an error message showing the expected syntax, changes no data and continues running

#### Scenario: Delivery 2 command recognised
- **WHEN** the line `attraction status;A01` is processed
- **THEN** it is handled as the `ATTRACTION STATUS` command with argument `A01`

### Requirement: File mode
When the JAR is started with a file path as its first argument (or as the argument following `--fly`), the application SHALL process the commands contained in that file, one command per line, in order of appearance, ignoring blank lines. Command handling SHALL be identical to interactive mode. A nonexistent or unreadable file SHALL be reported with an error message and SHALL NOT produce an uncontrolled failure (no stack trace). Without `--fly`, argument handling SHALL remain as in Delivery 1.

#### Scenario: Commands executed in order
- **WHEN** the JAR is started with a file containing a `USER CREATE` line, a blank line and a `USER UPDATE` line for the created user
- **THEN** the user is created and then updated, and the blank line is ignored

#### Scenario: Missing command file
- **WHEN** the JAR is started with the path of a file that does not exist
- **THEN** the application prints an error message naming the file and exits with a non-zero status without a stack trace

#### Scenario: Command file after --fly
- **WHEN** the JAR is started with `--fly input_fase2.txt`
- **THEN** the commands in `input_fase2.txt` are processed in temporary persistence mode

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, covering at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP` and `EXIT`.

#### Scenario: Show help
- **WHEN** the command `HELP` is processed
- **THEN** the syntax of every available command is printed and the application keeps running

#### Scenario: Help lists Delivery 2 commands
- **WHEN** the command `HELP` is processed
- **THEN** the output includes `GROUPTICKET CREATE;<identifier>;<group>;<date>` and `ATTRACTION STATUS;<attractionIdentifier>`
