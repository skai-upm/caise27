# Spec Delta

## MODIFIED Requirements

### Requirement: Executable JAR build
The project SHALL be a standard Maven project targeting Java 21 that builds with `mvn package` and produces an executable JAR in `target/` that starts with `java -jar` without an IDE, extra classpath entries or source changes. At runtime only the Java standard library and the BCrypt library declared in Maven SHALL be used, and the JAR SHALL bundle that library.

#### Scenario: Build and start the JAR
- **WHEN** `mvn package` is run on a clean checkout and the produced JAR is started with `java -jar target/<app>.jar`
- **THEN** the build succeeds and the application starts and waits for commands on standard input

#### Scenario: Self-contained JAR
- **WHEN** the JAR is copied alone to another directory and a `USER CREATE` command is run with it
- **THEN** the user is created without class-loading errors

### Requirement: Command syntax
The application SHALL interpret each input line as a command whose elements are separated by the `;` character. The first element SHALL identify the command (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`, `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, `INVOICE CREATE`, `HELP`, `HELP EXTENDED`, `EXIT`). Command keywords SHALL be recognised regardless of letter case and surrounding whitespace. Parsing SHALL use basic String operations and `split`, without regular expressions or external libraries.

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

#### Scenario: Delivery 3 command recognised
- **WHEN** the line `speedpass create;SP01;T001;1` is processed
- **THEN** it is handled as the `SPEEDPASS CREATE` command

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, covering at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Show help
- **WHEN** the command `HELP` is processed
- **THEN** the syntax of every available command is printed and the application keeps running

#### Scenario: Help lists Delivery 2 commands
- **WHEN** the command `HELP` is processed
- **THEN** the output includes `GROUPTICKET CREATE;<identifier>;<group>;<date>` and `ATTRACTION STATUS;<attractionIdentifier>`

#### Scenario: Help lists Delivery 3 commands
- **WHEN** the command `HELP` is processed
- **THEN** the output includes `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `INVOICE CREATE;GROUP;<groupIdentifier>` and `HELP EXTENDED`

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display every command's syntax together with a brief description of its purpose.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each command syntax shown by `HELP` appears with an explanatory description
