# command-interface Specification

## Purpose

Defines how the amusement park application is built, started and driven through `;`-separated text commands, either interactively or from a command file.

## Requirements

### Requirement: Executable JAR build
The project SHALL be a standard Maven project targeting Java 21 whose `mvn package` build produces an executable JAR that starts the application with `java -jar`.

#### Scenario: Build and run
- **WHEN** `mvn package` is run on a clean checkout
- **THEN** an executable JAR is produced under `target/` and `java -jar <jar>` starts the application without additional classpath configuration

### Requirement: Interactive mode
When started without arguments the application SHALL read commands from standard input, one per line, processing each and then waiting for the next until `EXIT` is received or input ends.

#### Scenario: Commands from standard input
- **WHEN** the JAR is run without arguments and the user types `HELP` followed by `EXIT`
- **THEN** the help text is shown and the application terminates normally

#### Scenario: End of input
- **WHEN** standard input is closed without an `EXIT` command
- **THEN** the application terminates normally

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

### Requirement: Command syntax
Commands SHALL consist of fields separated by the `;` character, the first field being the command name (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`). Command names SHALL be matched ignoring letter case and surrounding whitespace; field values SHALL be trimmed of surrounding whitespace.

#### Scenario: Recognised command
- **WHEN** the line `user create;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** it is handled as a `USER CREATE` command

### Requirement: Invalid command handling
An unknown command, a command with the wrong number of fields, or any rejected operation SHALL produce an error message and SHALL NOT terminate the application.

#### Scenario: Unknown command
- **WHEN** the line `FOO BAR` is processed
- **THEN** an error message is printed and the next command is processed normally

#### Scenario: Wrong number of fields
- **WHEN** the line `USER DELETE;ana@correo.com` is processed
- **THEN** an error message describing the expected syntax is printed and the application keeps running

### Requirement: HELP command
The `HELP` command SHALL display the syntax of every command recognised by the application across Deliveries 1, 2 and 3, including `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP EXTENDED`, `HELP` and `EXIT`.

#### Scenario: Show help
- **WHEN** `HELP` is processed
- **THEN** the output lists the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`

#### Scenario: Delivery 2 commands in help
- **WHEN** `HELP` is processed
- **THEN** the output also lists the syntax of every `SYSTEM`, `GROUP`, `TICKET`, `GROUPTICKET` and `ATTRACTION` command

#### Scenario: Delivery 3 commands in help
- **WHEN** `HELP` is processed
- **THEN** the output also lists the syntax of the typed `ATTRACTION CREATE`, `ATTRACTION ENTER ... ;SPEED`, every `SPEEDPASS`, `SPECIALPASS` and `INVOICE` command, and `HELP EXTENDED`

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner; no command after `EXIT` SHALL be executed, in either execution mode.

#### Scenario: Exit stops file processing
- **WHEN** a command file contains `USER CREATE;...`, then `EXIT`, then another `USER CREATE;...`
- **THEN** only the first user is created and the application terminates after `EXIT`

### Requirement: Extended help
The `HELP EXTENDED` command SHALL display, for every command shown by `HELP`, its syntax together with a brief description of its purpose.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each command's syntax is shown with a one-line explanation of what it does
