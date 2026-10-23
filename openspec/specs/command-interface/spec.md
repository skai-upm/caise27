# command-interface Specification

## Purpose

Defines the text-command language of the application and how commands are read, interpreted, and executed from standard input or from a command file.

## Requirements

### Requirement: Semicolon-separated command syntax
Each command SHALL be one line of `;`-separated elements. The first element is the command keyword: one of `USER CREATE|DELETE|UPDATE`, `SYSTEM DATE|PRICE`, `GROUP CREATE|ADD|REMOVE|DELETE`, `TICKET CREATE|DELETE`, `GROUPTICKET CREATE|DELETE`, `SPEEDPASS CREATE|DELETE`, `SPECIALPASS CREATE|DELETE`, `ATTRACTION CREATE|ENTER|EXIT|LIST|STATUS`, `INVOICE CREATE`, `HELP`, `HELP EXTENDED`, and `EXIT`. Whitespace around elements SHALL be ignored, and keywords matched case-insensitively. Parsing SHALL use only basic `String` operations and `split`.

#### Scenario: Recognized command with arguments
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is interpreted as the `USER DELETE` command with arguments `ana@correo.com` and `Clave1!`

#### Scenario: Surrounding whitespace is ignored
- **WHEN** the line `  help  ` is processed
- **THEN** it is interpreted as the `HELP` command

#### Scenario: Delivery 2 keyword recognized
- **WHEN** the line `attraction status;A01` is processed
- **THEN** it is interpreted as the `ATTRACTION STATUS` command with argument `A01`

#### Scenario: Delivery 3 keyword recognized
- **WHEN** the line `speedpass create;SP01;T001;1` is processed
- **THEN** it is interpreted as the `SPEEDPASS CREATE` command with arguments `SP01`, `T001`, and `1`

### Requirement: Invalid commands are reported without terminating
An unknown keyword, a wrong number of arguments, or any invalid argument SHALL produce an error message beginning with `ERROR` and SHALL NOT terminate the application. Processing then continues with the next command.

#### Scenario: Unknown command
- **WHEN** the line `FOO;bar` is processed
- **THEN** an error message beginning with `ERROR` is printed and the application waits for or reads the next command

#### Scenario: Wrong number of arguments
- **WHEN** the line `USER DELETE;ana@correo.com` is processed
- **THEN** an error message beginning with `ERROR` that shows the expected syntax is printed and the application continues

### Requirement: Identical processing in both input modes
Commands SHALL be processed by the same logic, with the same observable output, whether they come from standard input or from a command file.

#### Scenario: Same output from file and standard input
- **WHEN** the same sequence of commands is supplied once via standard input and once via a command file, starting from the same stored users
- **THEN** the printed results and the resulting stored users are identical

### Requirement: Interactive mode
In interactive mode the application SHALL read and execute one command per line from standard input, waiting for the next line after each command, until `EXIT` is processed or standard input ends.

#### Scenario: Continues after each command
- **WHEN** the user enters `HELP` and then `EXIT` on standard input
- **THEN** the help text is printed, and the application terminates only after `EXIT`

#### Scenario: End of input terminates cleanly
- **WHEN** standard input is closed without an `EXIT` command
- **THEN** the application terminates normally

### Requirement: File mode
In file mode the application SHALL execute the commands in the given file, one per line, in the order they appear, ignoring blank lines. A nonexistent or unreadable file SHALL produce an error message and a controlled termination, never a stack trace.

#### Scenario: Commands run in order
- **WHEN** a file containing a `USER CREATE` line followed by a `USER UPDATE` line for the same user is processed
- **THEN** the user is first created and then updated

#### Scenario: Blank lines ignored
- **WHEN** the command file contains empty or whitespace-only lines between commands
- **THEN** those lines produce no output and no error

#### Scenario: Missing file
- **WHEN** the application is started with a path to a file that does not exist
- **THEN** it prints an error message beginning with `ERROR` naming the file and terminates without a stack trace

### Requirement: HELP command
`HELP` SHALL print the syntax of every command of all deliveries. That includes every Delivery 1 and 2 syntax, `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, and `HELP EXTENDED`.

#### Scenario: Help lists all commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, and `EXIT`

#### Scenario: Help lists Delivery 2 commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains the syntax of every `SYSTEM`, `GROUP`, `TICKET`, `GROUPTICKET`, and `ATTRACTION` command

#### Scenario: Help lists Delivery 3 commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains every `SPEEDPASS`, `SPECIALPASS`, and `INVOICE CREATE` syntax, the typed `ATTRACTION CREATE`, `ATTRACTION ENTER` with `SPEED`, and `HELP EXTENDED`

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner. In file mode, no line after `EXIT` SHALL be processed.

#### Scenario: Lines after EXIT are ignored in file mode
- **WHEN** a command file contains `USER CREATE;...` for user A, then `EXIT`, then `USER CREATE;...` for user B
- **THEN** user A is created, the application terminates, and user B is not created

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL print, for every recognized command syntax, the syntax together with a short description of the command's purpose. It SHALL change no state.

#### Scenario: Extended help describes commands
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every syntax listed by `HELP` appears, each followed by a non-empty description
