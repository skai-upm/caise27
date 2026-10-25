# command-processing Specification

## Purpose

Defines how the application receives and interprets text commands, both interactively and from an input file, including the general HELP and EXIT commands and error tolerance.

## Requirements

### Requirement: Semicolon-separated command format
Each command SHALL be a single line whose components are separated by `;`. The first component identifies the operation (for example `USER CREATE`) and the remaining components are its parameters. Commands SHALL be interpreted using `String.split`, without regular expressions or external libraries.

#### Scenario: Command with parameters
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the operation `USER DELETE` is executed with parameters `ana@correo.com` and `Clave1!`

### Requirement: Invalid commands do not terminate the application
The application SHALL report an error for an unknown command or a command with a wrong number of parameters and SHALL continue processing subsequent commands.

#### Scenario: Unknown command
- **WHEN** the line `FOO BAR;1` is processed
- **THEN** an error message is printed and the application keeps accepting commands

#### Scenario: Insufficient parameters
- **WHEN** the line `USER CREATE;Ana` is processed
- **THEN** an error message reporting the wrong number of parameters is printed, no user is created, and the application keeps accepting commands

### Requirement: Command results are reported
Every processed command SHALL print a result line: lines starting with `OK` for successful operations and lines starting with `ERROR` followed by a reason for rejected operations.

#### Scenario: Successful command
- **WHEN** a valid `USER CREATE` command is processed
- **THEN** a line starting with `OK` is printed

#### Scenario: Rejected command
- **WHEN** a `USER DELETE` command with a wrong password is processed
- **THEN** a line starting with `ERROR` is printed

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands line by line from standard input, processing each line as a separate command, until it receives `EXIT` or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application is started without arguments and receives `HELP` and then `EXIT` on standard input
- **THEN** it prints the help, then terminates in a controlled manner

#### Scenario: End of standard input
- **WHEN** standard input ends without an `EXIT` command
- **THEN** the application terminates in a controlled manner

### Requirement: File mode
When started with a non-option argument, the application SHALL interpret the first such argument as the path to a text file containing one command per line, and SHALL execute those commands in the order they appear. The `--fly` option MAY appear before the file path. Unknown options or more than one file path SHALL be reported as an error and the application SHALL terminate in a controlled manner.

#### Scenario: Commands executed in order
- **WHEN** the application is started with a file containing a `USER CREATE` line followed by a `USER UPDATE` line for the same user
- **THEN** the user is created first and then updated

#### Scenario: Fly option with a file
- **WHEN** the application is started with arguments `--fly input_fase2.txt`
- **THEN** the commands of `input_fase2.txt` are executed

#### Scenario: Invalid arguments
- **WHEN** the application is started with an unknown option such as `--foo` or with two file paths
- **THEN** an error message is printed and no command is executed

### Requirement: Blank lines are ignored
Blank lines (empty or containing only whitespace) SHALL be ignored without producing errors.

#### Scenario: File with blank lines
- **WHEN** a command file contains a blank line between two valid commands
- **THEN** both commands are executed and no error is printed for the blank line

### Requirement: Shared processing for both modes
Interactive mode and file mode SHALL use the same command-processing mechanism, so the same command produces the same result whichever source supplied it.

#### Scenario: Same result from file and terminal
- **WHEN** the same sequence of commands is supplied once through standard input and once through a file, starting from the same stored data
- **THEN** both executions print the same results and leave the same stored data

### Requirement: HELP command
The `HELP` command SHALL display every registered command with its basic syntax (optional parameters in brackets), generated from the registered commands. It SHALL include at least the E1/E2 commands plus `INVOICE CREATE;USER;Correo`, `INVOICE CREATE;GROUP;IdGrupo`, `SPEEDPASS CREATE;IdEntrada;Cantidad`, `SPEEDPASS DELETE;IdSpeedPass`, `SPECIALPASS CREATE;IdEntrada`, `SPECIALPASS DELETE;IdEntrada`, `ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;Tipo]`, `ATTRACTION ENTER;IdAtraccion;IdEntrada[;SPEED]` and `HELP EXTENDED`.

#### Scenario: Display help
- **WHEN** the `HELP` command is processed
- **THEN** the syntax of every available command is printed

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner. In file mode, processing SHALL stop at `EXIT` and no later command in the file SHALL be executed.

#### Scenario: EXIT in interactive mode
- **WHEN** `EXIT` is entered in interactive mode
- **THEN** the application stops reading commands and terminates

#### Scenario: Commands after EXIT in a file are ignored
- **WHEN** a command file contains `USER CREATE` for user A, then `EXIT`, then `USER CREATE` for user B
- **THEN** user A is created and user B is not

### Requirement: Invalid input file handling
If the file argument does not exist, cannot be opened or cannot be read, the application SHALL print an error message and terminate in a controlled manner, without showing an unhandled Java exception.

#### Scenario: Nonexistent file
- **WHEN** the application is started with the path of a file that does not exist
- **THEN** an error message naming the problem is printed and the application terminates without a stack trace

### Requirement: HELP EXTENDED command
The `HELP EXTENDED` command SHALL display, for every registered command, its syntax and a brief description of its behavior.

#### Scenario: Display extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each registered command is printed with its syntax followed by a description

### Requirement: Pluggable help formatting
Both help modes SHALL be produced from the same registered-command information by a help generator that receives its formatting behavior through dependency injection, so new help formats can be added without changing command processing.

#### Scenario: New command appears in both helps
- **WHEN** a command is registered
- **THEN** it appears in both `HELP` and `HELP EXTENDED` without changes to the help generator

### Requirement: Optional parameters
Commands MAY declare optional trailing parameters; a command SHALL be accepted when the number of parameters is between its minimum and maximum, and rejected with an error showing its syntax otherwise.

#### Scenario: Optional parameter omitted
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** it is accepted although the type parameter is omitted

#### Scenario: Too many parameters
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED;X` is processed
- **THEN** an `ERROR` result showing the command syntax is printed
