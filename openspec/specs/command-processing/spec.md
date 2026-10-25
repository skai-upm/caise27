# command-processing Specification

## Purpose

Defines how text commands reach the application, interactively or from a command file, and how they are parsed, dispatched and reported, including HELP and EXIT.

## Requirements

### Requirement: Command parsing with String.split
Each command SHALL be a single line of text whose components are separated by `;` and interpreted using `String.split`; the first component identifies the operation (e.g. `USER CREATE`). No external library SHALL be used to interpret commands.

#### Scenario: Operation and parameters are recognised
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is interpreted as operation `USER DELETE` with parameters `ana@correo.com` and `Clave1!`

### Requirement: Invalid commands do not terminate the program
Unknown commands, commands with insufficient or extra parameters, and unexpected errors while executing a command SHALL be reported with an error message and SHALL NOT terminate the application.

#### Scenario: Unknown command
- **WHEN** the line `FOO BAR` is processed
- **THEN** an error message is shown and the next command is processed normally

#### Scenario: Insufficient parameters
- **WHEN** the line `USER UPDATE;ana@correo.com` is processed
- **THEN** an error message is shown and the application keeps running

### Requirement: Interactive mode
When started without a command-file argument (no arguments, or only the `--fly` option), the application SHALL read commands line by line from standard input and process each one, until it receives `EXIT` or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application is started without arguments and receives `HELP` then `EXIT` on standard input
- **THEN** it prints the help text and terminates after `EXIT`

#### Scenario: End of input
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates in a controlled manner

### Requirement: File mode
When started with a command-file argument (the first argument, or the argument following the `--fly` option), the application SHALL treat it as the path of a text file containing one command per line, read it line by line and execute the commands in order, using exactly the same command processing as interactive mode.

#### Scenario: Commands from file
- **WHEN** the application is started with a file containing two `USER CREATE` lines and a `USER UPDATE` line
- **THEN** the three commands are executed in order with the same results they would have interactively

### Requirement: Blank lines ignored
Blank lines (empty or whitespace-only) SHALL be ignored without producing errors.

#### Scenario: File with blank lines
- **WHEN** a command file contains blank lines between commands
- **THEN** only the non-blank lines are processed and no error is reported for the blank ones

### Requirement: Invalid command file
If the supplied file does not exist, cannot be opened or cannot be read, the application SHALL report the problem and terminate in a controlled manner without displaying an unhandled Java exception.

#### Scenario: Nonexistent file
- **WHEN** the application is started with the argument `missing.txt` that does not exist
- **THEN** an error message naming the file is printed and the application ends without a stack trace

### Requirement: HELP command
`HELP` SHALL display the syntax of every registered command: the USER, SYSTEM, GROUP, TICKET, GROUPTICKET, ATTRACTION, INVOICE, SPEEDPASS and SPECIALPASS commands, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** the output lists the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION DELETE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION STATUS`, `INVOICE CREATE`, `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, `HELP`, `HELP EXTENDED` and `EXIT`

### Requirement: EXIT command
`EXIT` SHALL terminate command processing in a controlled manner; no command after `EXIT` SHALL be executed, in either mode.

#### Scenario: Commands after EXIT in a file
- **WHEN** a command file contains `USER CREATE` for Ana, then `EXIT`, then `USER CREATE` for Pedro
- **THEN** Ana is created, processing stops at `EXIT` and Pedro is not created

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, for every registered command, its syntax and a brief description of its behavior. Both help modes SHALL be generated from the same registered command information, by a help component that receives its output-formatting behavior through dependency injection.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command from `HELP` appears with its syntax followed by a description
