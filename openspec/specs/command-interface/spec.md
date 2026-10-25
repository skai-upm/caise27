# command-interface Specification

## Purpose

Defines how text commands reach the application, interactively or from a file, how they are parsed, and the general HELP and EXIT commands.

## Requirements

### Requirement: Command parsing
Each command SHALL be a single line whose components are separated by `;` and obtained with `String.split`; the first component holds the command name (for example `USER CREATE`). Unknown commands and commands with an incorrect number of parameters SHALL be reported as errors without terminating the application.

#### Scenario: Unknown command
- **WHEN** the command `FOO BAR;1` is processed
- **THEN** the system reports an error and continues accepting commands

#### Scenario: Insufficient parameters
- **WHEN** the command `USER DELETE;ana@correo.com` is processed
- **THEN** the system reports an error and continues accepting commands

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands line by line from standard input and process each one until it receives `EXIT` or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application is started without arguments and receives `HELP` and then `EXIT` on standard input
- **THEN** it prints the help text and terminates

#### Scenario: End of input
- **WHEN** standard input ends without an `EXIT` command
- **THEN** the application terminates normally

### Requirement: File mode
When started with one argument, the application SHALL treat it as the path of a text file containing one command per line, and SHALL execute the commands in file order using exactly the same command processing as interactive mode.

#### Scenario: Commands from file
- **WHEN** the application is started with `input_basico.txt` containing two `USER CREATE` lines, one `USER UPDATE` line and `EXIT`
- **THEN** both users are created and updated exactly as if those lines were typed interactively

### Requirement: Blank lines are ignored
Blank lines (empty or whitespace only) SHALL be ignored in both modes without producing errors.

#### Scenario: Blank line in file
- **WHEN** a command file contains a blank line between two valid commands
- **THEN** both commands are executed and no error is reported for the blank line

### Requirement: EXIT command
The `EXIT` command SHALL terminate command processing in a controlled manner; no command after `EXIT` SHALL be executed.

#### Scenario: Commands after EXIT in file
- **WHEN** a command file contains `USER CREATE` for Ana, `EXIT`, then `USER CREATE` for Pedro
- **THEN** Ana is created and Pedro is not

### Requirement: HELP command
The `HELP` command SHALL display every currently available command with its basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`, the Delivery 2 commands `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION DELETE`, `ATTRACTION ENTER`, `ATTRACTION EXIT` and `ATTRACTION STATUS`, and the Delivery 3 commands `INVOICE CREATE`, `HELP EXTENDED`, `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE` and the typed and SPEED variants of `ATTRACTION CREATE` and `ATTRACTION ENTER`.

#### Scenario: Help lists commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`

#### Scenario: Help lists Delivery 2 commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of every system, group, ticket, group ticket and attraction command

#### Scenario: Help lists Delivery 3 commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of every invoice and pass command, `HELP EXTENDED`, `ATTRACTION CREATE` with a type and `ATTRACTION ENTER` with `SPEED`

### Requirement: Invalid command file
If the file argument does not exist, cannot be opened or cannot be read, the application SHALL report the problem with a readable message and terminate in a controlled manner, without displaying an unhandled Java exception.

#### Scenario: Nonexistent file
- **WHEN** the application is started with `no_existe.txt` and that file does not exist
- **THEN** it prints an error message naming the file and terminates without a stack trace

### Requirement: HELP EXTENDED command
The `HELP EXTENDED` command SHALL display, for every registered command, its syntax and a brief description of its behaviour. `HELP` and `HELP EXTENDED` SHALL be generated from the same list of registered commands, and the help builder SHALL receive its output-formatting behaviour by dependency injection.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command syntax shown by `HELP` appears together with a description
