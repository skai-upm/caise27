# command-processing Specification

## Purpose

Defines how the application receives textual commands from standard input or from a command file, processes them through one shared command path, and provides the general HELP and EXIT commands.

## Requirements

### Requirement: Command input sources
The application SHALL read commands one per line from standard input when started without arguments, and from the text file whose path is the first argument when one is given. Commands SHALL be processed in the order they are read, and the observable behaviour of each command SHALL be the same for both sources.

#### Scenario: Commands from standard input
- **WHEN** the application starts without arguments and receives `HELP` on standard input
- **THEN** it prints the help text and waits for the next command

#### Scenario: Commands from a file
- **WHEN** the application starts with a file argument whose first line is `HELP`
- **THEN** it prints the same help text as in interactive mode

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least: `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `HELP` and `EXIT`, together with the commands of later deliveries listed in their own requirements.

#### Scenario: Help lists the required commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `HELP` and `EXIT`

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner, and no command received after `EXIT` SHALL be executed, regardless of the input source.

#### Scenario: Exit in interactive mode
- **WHEN** the user enters `EXIT` on standard input
- **THEN** the application stops reading commands and ends normally

#### Scenario: Exit stops file processing
- **WHEN** a command file contains a command, then `EXIT`, then another command
- **THEN** the command before `EXIT` is processed and the line after `EXIT` is not processed

#### Scenario: End of input without EXIT
- **WHEN** the input ends without an `EXIT` command
- **THEN** the application ends normally after processing all commands

### Requirement: Unrecognised commands
A line that does not match an available command SHALL be rejected with an error message, without terminating the application.

#### Scenario: Unknown command is rejected
- **WHEN** the user enters `FOO`
- **THEN** an error message indicating that the command is not recognised is printed and the next command is still processed

### Requirement: Semicolon-separated commands
The elements of a command SHALL be separated by the `;` character, and commands SHALL be interpreted with basic String operations including `split`, without external libraries. The first element identifies the command (for example `USER CREATE`).

#### Scenario: Command name and arguments
- **WHEN** the line `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** it is handled as the `USER CREATE` command with the seven following elements as its arguments

### Requirement: Incorrect commands do not terminate the application
An incorrect command (unknown name, wrong number of elements or invalid values) SHALL be reported with an error message and SHALL NOT cause the application to terminate unexpectedly.

#### Scenario: Malformed command followed by a valid one
- **WHEN** `USER CREATE;Ana` is processed and then `HELP` is processed
- **THEN** an error is printed for the first line and the help text is printed for the second

### Requirement: Command file processing order
When a command file is given as the first argument, the application SHALL process its commands one per line in the order in which they appear, ignoring blank lines, using the same command processing as interactive mode so that each command's observable behaviour is identical in both modes.

#### Scenario: Source example processed in order
- **WHEN** a file contains `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` and `EXIT`, one per line
- **THEN** the output shows `USER CREATED`, `USER CREATED`, `USER UPDATED` in that order and the application ends

#### Scenario: Blank lines ignored
- **WHEN** the command file contains blank lines between commands
- **THEN** the blank lines produce no output and do not affect the other commands

#### Scenario: Same output in both modes
- **WHEN** the same sequence of commands is given once on standard input and once as a command file, each starting from the same stored data
- **THEN** both executions print the same output

#### Scenario: Interactive mode keeps waiting
- **WHEN** a command is entered in interactive mode
- **THEN** after processing it the application waits for the next command until `EXIT` or end of input

### Requirement: Controlled handling of command file errors
A nonexistent or unreadable command file SHALL be handled in a controlled manner: the application SHALL print an error message naming the file, SHALL NOT process any command and SHALL end normally without an unhandled exception.

#### Scenario: Nonexistent file
- **WHEN** the application is started with `missing.txt` and that file does not exist
- **THEN** an error stating that the command file was not found is printed and the application ends normally

#### Scenario: Unreadable file
- **WHEN** the application is started with a path that exists but cannot be read as a file (for example a directory)
- **THEN** an error stating that the command file cannot be read is printed and the application ends normally

### Requirement: Unexpected errors are contained
Any unexpected error raised while processing a single command SHALL be reported as an error message, and the application SHALL continue with the next command instead of terminating.

#### Scenario: Internal failure during a command
- **WHEN** processing a command fails with an unexpected runtime error
- **THEN** an error message is printed and the following command is still processed

### Requirement: Delivery 1 example flow
The application SHALL execute the assignment's basic example — creating Ana and Pedro, updating Ana's phone and email, deleting Pedro, HELP and EXIT — with the same results interactively and from a command file.

#### Scenario: Example from a command file
- **WHEN** a file with `USER CREATE` (Ana), `USER CREATE` (Pedro), `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com`, `USER DELETE;pedro@correo.com;Otra2#`, `HELP` and `EXIT` is processed
- **THEN** the output shows `USER CREATED`, `USER CREATED`, `USER UPDATED`, `USER UPDATED`, `USER DELETED`, the help text, and the application ends with only Ana (email `ana.nueva@correo.com`, phone `699999999`) stored

#### Scenario: Example interactively
- **WHEN** the same commands are typed on standard input from the same starting data
- **THEN** the output and stored users are identical to the file execution

### Requirement: HELP lists system commands
The HELP output SHALL also include `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`.

#### Scenario: Help shows system commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>` in addition to the Delivery 1 commands

### Requirement: HELP lists group commands
The HELP output SHALL also include `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>` and `GROUP DELETE;<Identifier>`.

#### Scenario: Help shows group commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains the four GROUP command syntaxes

### Requirement: HELP lists ticket commands
The HELP output SHALL also include `TICKET CREATE;<identifier>;<email>;<date>` and `TICKET DELETE;<identifier>`.

#### Scenario: Help shows ticket commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains `TICKET CREATE;<identifier>;<email>;<date>` and `TICKET DELETE;<identifier>`

### Requirement: HELP lists group ticket commands
The HELP output SHALL also include `GROUPTICKET CREATE;<identifier>;<group>;<date>` and `GROUPTICKET DELETE;<identifier>`.

#### Scenario: Help shows group ticket commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains `GROUPTICKET CREATE;<identifier>;<group>;<date>` and `GROUPTICKET DELETE;<identifier>`

### Requirement: HELP lists attraction creation and listing
The HELP output SHALL also include `ATTRACTION CREATE;<identifier>;<name>;<capacity>` and `ATTRACTION LIST;<attraction>`.

#### Scenario: Help shows attraction commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains `ATTRACTION CREATE;<identifier>;<name>;<capacity>` and `ATTRACTION LIST;<attraction>`

### Requirement: HELP lists attraction entry
The HELP output SHALL also include `ATTRACTION ENTER;<attraction>;<ticket>`.

#### Scenario: Help shows attraction entry
- **WHEN** the user enters `HELP`
- **THEN** the output contains `ATTRACTION ENTER;<attraction>;<ticket>`

### Requirement: HELP lists attraction exit and status
The HELP output SHALL also include `ATTRACTION EXIT;<attraction>;<email>` and `ATTRACTION STATUS;<attractionIdentifier>`.

#### Scenario: Help shows exit and status
- **WHEN** the user enters `HELP`
- **THEN** the output contains `ATTRACTION EXIT;<attraction>;<email>` and `ATTRACTION STATUS;<attractionIdentifier>`

### Requirement: Execution forms with temporary mode
The application SHALL support the execution forms `java -jar target/amusement-park.jar`, `java -jar target/amusement-park.jar input_fase2.txt`, `java -jar target/amusement-park.jar --fly` and `java -jar target/amusement-park.jar --fly input_fase2.txt`. `--fly` SHALL only be recognised as the first argument; without it, argument handling SHALL remain as in Delivery 1.

#### Scenario: Fly with command file
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** the commands of `input_fase2.txt` are processed in temporary mode

#### Scenario: Fly interactively
- **WHEN** the application is started with `--fly` only
- **THEN** commands are read from standard input in temporary mode

#### Scenario: Normal file mode unchanged
- **WHEN** the application is started with `input_fase2.txt` only
- **THEN** the commands are processed with normal persistence

### Requirement: Delivery 2 commands coexist
All Delivery 1 and Delivery 2 commands SHALL work together in one session, from standard input or a command file, with and without `--fly`.

#### Scenario: Complete Delivery 2 session from a file
- **WHEN** a command file creates users, sets the system date and price, creates a group with members, buys an individual ticket and a group ticket, creates attractions, enters with both tickets, exits one user, queries status and list, and ends with `EXIT`
- **THEN** every operation succeeds with the expected prices and attraction status, and the same file run with `--fly` produces the same output and leaves no temporary data

#### Scenario: Rejected operations in the session
- **WHEN** the session also tries to delete a ticket for the current date, modify a group locked by an active group ticket, delete a user with an active ticket, and enter an attraction without enough capacity for a group
- **THEN** each is rejected with an error and the session continues

### Requirement: Behaviour preserved after reorganisation
After the layered reorganisation every Delivery 1 and Delivery 2 command SHALL produce the same outputs, persisted data and execution forms as before.

#### Scenario: Delivery 2 session unchanged
- **WHEN** the Delivery 2 integration session is executed after the reorganisation, in normal and `--fly` modes
- **THEN** the output and persisted files are identical to those produced before the reorganisation

### Requirement: HELP lists invoice commands
The HELP output SHALL also include `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`.

#### Scenario: Help shows invoice commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`

### Requirement: Extended help
`HELP EXTENDED` SHALL display, for every command recognised by the application, its syntax together with a brief explanation of its purpose. `HELP` SHALL remain available and keep displaying the syntax of the recognised commands, including `HELP EXTENDED`.

#### Scenario: Extended help content
- **WHEN** the user enters `HELP EXTENDED`
- **THEN** every command syntax shown by `HELP` appears with a description of its purpose (for example `TICKET CREATE;<identifier>;<email>;<date>` with an explanation that it buys an individual ticket)

#### Scenario: Plain help unchanged
- **WHEN** the user enters `HELP`
- **THEN** the syntax of every command is shown without descriptions, including `HELP EXTENDED`

#### Scenario: Same behaviour from a file
- **WHEN** `HELP EXTENDED` is read from a command file
- **THEN** the same extended help is displayed and processing continues

### Requirement: Injected help generation behaviour
The component that generates help text SHALL receive, through dependency injection, the behaviour used to build its output, so that new help formats can be added without changing the logic that reads and executes commands.

#### Scenario: Alternative format injected
- **WHEN** a help generator is created with a different formatting behaviour
- **THEN** it produces the help in that format from the same command information, with no change to command processing
