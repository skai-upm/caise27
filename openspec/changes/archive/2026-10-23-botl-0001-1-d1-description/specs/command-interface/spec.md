# Spec Delta

## Purpose

Defines the text-command language of the application and how commands are read, interpreted, and executed from standard input or from a command file.

## ADDED Requirements

### Requirement: Semicolon-separated command syntax
Each command SHALL be one line whose elements are separated by `;`. The first element is the command keyword (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`). Leading and trailing whitespace around each element SHALL be ignored, and keywords SHALL be matched case-insensitively. Parsing SHALL use only basic `String` operations and `split`.

#### Scenario: Recognized command with arguments
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is interpreted as the `USER DELETE` command with arguments `ana@correo.com` and `Clave1!`

#### Scenario: Surrounding whitespace is ignored
- **WHEN** the line `  help  ` is processed
- **THEN** it is interpreted as the `HELP` command

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
The `HELP` command SHALL print the available commands and their syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP`, and `EXIT`.

#### Scenario: Help lists all commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, and `EXIT`

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner. In file mode, no line after `EXIT` SHALL be processed.

#### Scenario: Lines after EXIT are ignored in file mode
- **WHEN** a command file contains `USER CREATE;...` for user A, then `EXIT`, then `USER CREATE;...` for user B
- **THEN** user A is created, the application terminates, and user B is not created
