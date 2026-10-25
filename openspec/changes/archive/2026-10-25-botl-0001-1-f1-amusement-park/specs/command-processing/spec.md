# Spec Delta

## Purpose

Defines how text commands reach the application, interactively or from a command file, and how they are parsed, dispatched and reported, including HELP and EXIT.

## ADDED Requirements

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
When started without arguments, the application SHALL read commands line by line from standard input and process each one, until it receives `EXIT` or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application is started without arguments and receives `HELP` then `EXIT` on standard input
- **THEN** it prints the help text and terminates after `EXIT`

#### Scenario: End of input
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates in a controlled manner

### Requirement: File mode
When started with one argument, the application SHALL treat it as the path of a text file containing one command per line, read it line by line and execute the commands in order, using exactly the same command processing as interactive mode.

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
`HELP` SHALL display the commands currently available and their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** the output lists the syntax of all five commands

### Requirement: EXIT command
`EXIT` SHALL terminate command processing in a controlled manner; no command after `EXIT` SHALL be executed, in either mode.

#### Scenario: Commands after EXIT in a file
- **WHEN** a command file contains `USER CREATE` for Ana, then `EXIT`, then `USER CREATE` for Pedro
- **THEN** Ana is created, processing stops at `EXIT` and Pedro is not created
