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
When started with one argument, the application SHALL interpret the first argument as the path to a text file containing one command per line, and SHALL execute those commands in the order they appear.

#### Scenario: Commands executed in order
- **WHEN** the application is started with a file containing a `USER CREATE` line followed by a `USER UPDATE` line for the same user
- **THEN** the user is created first and then updated

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
The `HELP` command SHALL display the available commands with their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`.

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
