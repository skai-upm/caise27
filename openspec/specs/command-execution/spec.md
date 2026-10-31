# command-execution Specification

## Purpose

Defines how text commands are received, interpreted and executed, whether typed interactively or read from a command file, including the general HELP and EXIT commands and error handling.

## Requirements

### Requirement: Semicolon-delimited command parsing
Each command SHALL be a single line whose operation and parameters are separated by `;`. Commands SHALL be interpreted with basic Java `String` operations (including `String.split`) and without external parsing libraries or regular-expression-based validation.

#### Scenario: Command split into operation and parameters
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is interpreted as operation `USER DELETE` with parameters `ana@correo.com` and `Clave1!`

### Requirement: Invalid commands are recoverable
An unknown command, a malformed command or a command with insufficient parameters SHALL be reported as an error without terminating the application, and subsequent commands SHALL still be processed.

#### Scenario: Unknown command in interactive session
- **WHEN** `FOO BAR` is entered followed by `HELP`
- **THEN** an error is reported for `FOO BAR` and `HELP` is still executed

### Requirement: Interactive mode
When launched without arguments, or with `--fly` as its only argument, the application SHALL read commands from standard input, one per line, processing each before reading the next, until `EXIT` is processed or input ends.

#### Scenario: Interactive session
- **WHEN** the application runs without arguments and receives `USER CREATE;...` (valid), `HELP` and `EXIT` on standard input
- **THEN** the user is created, help is shown, and the application ends after `EXIT`

#### Scenario: End of input
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates normally

#### Scenario: Interactive fly session
- **WHEN** the application runs with the single argument `--fly` and receives commands on standard input
- **THEN** the commands are processed interactively against temporary storage

### Requirement: File mode
When launched with a first argument other than `--fly`, the application SHALL treat it as a path to a text file; when the first argument is `--fly`, the second argument, if present, SHALL be that path. The application SHALL read the file line by line and execute each non-blank line as a command in file order. Blank lines SHALL be ignored.

#### Scenario: Ordered file processing with blank lines
- **WHEN** the file contains a valid `USER CREATE`, a blank line, `HELP` and `EXIT`
- **THEN** the user is created, help is shown, and the blank line causes no error

#### Scenario: File after --fly
- **WHEN** the application is launched with `--fly input_fase2.txt`
- **THEN** the commands of `input_fase2.txt` are executed in order

### Requirement: Shared command processing
Interactive mode and file mode SHALL use the same command interpretation and execution logic, so the same command yields the same observable result regardless of input source.

#### Scenario: Equivalent behavior across modes
- **WHEN** the same command sequence is supplied interactively and through a file with equivalent initial stored state
- **THEN** the output and resulting stored users are equivalent

### Requirement: Controlled handling of unreadable files
If the supplied file does not exist, cannot be opened or cannot be read, the application SHALL report the problem with a message and terminate in a controlled way, without showing an unhandled Java exception.

#### Scenario: Nonexistent file
- **WHEN** the application is launched with a path that does not exist
- **THEN** an error message naming the problem is printed and the application ends without a stack trace

### Requirement: HELP command
`HELP` SHALL display the currently available commands with their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;IdGrupo;Nombre`, `GROUP ADD;IdGrupo;Correo`, `GROUP REMOVE;IdGrupo;Correo`, `GROUP DELETE;IdGrupo`, `TICKET CREATE;IdTicket;Correo;yyyy-MM-dd`, `TICKET DELETE;IdTicket`, `GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;IdTicket`, `ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad`, `ATTRACTION ENTER;IdAtraccion;IdTicket`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `HELP` and `EXIT`.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is executed
- **THEN** every command above is shown with its syntax

### Requirement: EXIT command
`EXIT` SHALL end command processing in a controlled manner. In interactive mode no further input SHALL be read; in file mode no command after `EXIT` SHALL be executed.

#### Scenario: No commands after EXIT in a file
- **WHEN** a file contains a valid `USER CREATE` for Ana, then `EXIT`, then a valid `USER CREATE` for Pedro
- **THEN** only Ana is created and Pedro is not
