# command-processing Specification

## Purpose

Defines how the application reads, interprets and executes text commands, both interactively from standard input and from a command file, including HELP, EXIT and error tolerance.

## Requirements

### Requirement: Command syntax
Commands SHALL be single lines whose elements are separated by `;`, interpreted with basic String operations including `split`. The first element identifies the command; the recognised commands are `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`, `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, `INVOICE CREATE`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Recognised command is dispatched
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user deletion operation is executed with email `ana@correo.com` and password `Clave1!`

#### Scenario: Unknown command
- **WHEN** the line `USER LIST` is processed
- **THEN** an error message is shown and the application keeps running

#### Scenario: Wrong number of arguments
- **WHEN** the line `USER DELETE;ana@correo.com` is processed
- **THEN** an error message is shown, no operation is executed and the application keeps running

#### Scenario: Delivery 2 command is dispatched
- **WHEN** the line `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** the group creation operation is executed

#### Scenario: Delivery 3 command is dispatched
- **WHEN** the line `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** the SpeedPass purchase operation is executed

### Requirement: Robustness against incorrect input
An incorrect, malformed or rejected command MUST NOT terminate the application unexpectedly; the application SHALL report the problem and continue with the next command.

#### Scenario: Invalid command followed by a valid one
- **WHEN** a file contains `FOO` followed by `HELP`
- **THEN** an error is reported for `FOO` and the HELP output is still shown

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands from standard input, process each one and then wait for the next, until `EXIT` or end of input.

#### Scenario: Interactive session
- **WHEN** the application is started without arguments and receives `HELP` then `EXIT` on standard input
- **THEN** it shows the help text and terminates in a controlled manner

### Requirement: File mode
When started with a file path as first argument, the application SHALL process the commands in that file, one per line, in order of appearance, ignoring blank lines.

#### Scenario: Commands executed in order
- **WHEN** the file contains a USER CREATE for Ana followed by a USER UPDATE of Ana's PHONE
- **THEN** the user is created and then updated

#### Scenario: Blank lines ignored
- **WHEN** the file contains blank lines between commands
- **THEN** the blank lines produce no output and no error

#### Scenario: Nonexistent or unreadable file
- **WHEN** the application is started with a path that does not exist or cannot be read
- **THEN** it reports an error and terminates in a controlled manner without throwing an uncaught exception

### Requirement: Identical processing across modes
Commands SHALL be processed by the same logic regardless of whether they come from standard input or from a file, producing the same observable behaviour.

#### Scenario: Same output for same command
- **WHEN** the same command sequence is given interactively and via a file
- **THEN** the produced messages and resulting user data are the same

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Help lists commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of every Delivery 1, 2 and 3 command listed above, including HELP, HELP EXTENDED and EXIT

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner; once EXIT is processed no subsequent command is executed, in either mode.

#### Scenario: Commands after EXIT are not executed in file mode
- **WHEN** a file contains `USER CREATE;...` (Ana), `EXIT`, `USER CREATE;...` (Pedro)
- **THEN** Ana is created and Pedro is not

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, for every command shown by HELP, its syntax together with a brief explanation of its purpose. The help generator MUST receive the output-building behaviour by dependency injection, independent of command reading and execution.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command syntax appears with a description of its purpose

#### Scenario: Injected format
- **WHEN** a help generator is created with a different injected format
- **THEN** it produces output in that format without changes to command processing
