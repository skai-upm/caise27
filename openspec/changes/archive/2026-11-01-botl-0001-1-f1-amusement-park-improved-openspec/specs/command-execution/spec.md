# Spec Delta

## Purpose

Defines how text commands reach the application, interactively or from a file, through one shared processing path, including the general HELP and EXIT commands and error handling.

## ADDED Requirements

### Requirement: Semicolon command parsing
Each command line SHALL be split on `;` using `String.split` and basic `String` operations to obtain the operation and its parameters; no external command-parsing library or regular-expression matching SHALL be used.

#### Scenario: Operation and parameters extracted
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is interpreted as operation `USER DELETE` with parameters `ana@correo.com` and `Clave1!`

### Requirement: Interactive mode
When launched without arguments, the application SHALL read one command per line from standard input and process each, continuing until `EXIT` is processed or standard input ends.

#### Scenario: Commands read until EXIT
- **GIVEN** the application was started without arguments
- **WHEN** the lines `HELP` and then `EXIT` are entered
- **THEN** the help text is shown and the application then terminates normally

#### Scenario: End of input
- **GIVEN** the application was started without arguments
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates normally

### Requirement: File mode
When launched with an argument, the application SHALL treat the first argument as the path of a command file, read it line by line, and process each nonblank line as a command in file order. Blank lines SHALL be ignored.

#### Scenario: Ordered file processing with blank lines
- **GIVEN** a file containing valid commands interleaved with blank lines
- **WHEN** the application runs with that file as first argument
- **THEN** the nonblank commands are executed in file order and the blank lines cause no error

### Requirement: Shared processing across modes
Interactive and file modes SHALL use the same command interpretation and execution logic, so the same command yields the same result whichever way it is supplied.

#### Scenario: Equivalent results
- **GIVEN** the same command sequence and equivalent initial persistent state
- **WHEN** the sequence is supplied interactively or through a command file
- **THEN** the observable result of each command is the same

### Requirement: Invalid commands are recoverable
An unknown command, malformed line or insufficient parameters SHALL produce an error message without terminating the application, and processing SHALL continue with the next command.

#### Scenario: Unknown command in interactive mode
- **GIVEN** an active interactive session
- **WHEN** `FOO;bar` is entered followed by `HELP`
- **THEN** an error is reported for `FOO;bar` and the help text is still shown

### Requirement: Unreadable command file
If the command file does not exist, cannot be opened or cannot be read, the application SHALL report the problem and terminate in a controlled manner without displaying an unhandled exception.

#### Scenario: Nonexistent file
- **WHEN** the application is started with a path to a file that does not exist
- **THEN** an error message naming the problem is shown and the application ends without a stack trace

### Requirement: HELP command
`HELP` SHALL display the available commands with their basic syntax, at least: `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`, consistent with the implemented operations.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is executed
- **THEN** the five commands and their basic syntax are shown

### Requirement: EXIT command
`EXIT` SHALL end interactive execution in a controlled manner and SHALL stop file processing immediately so that no later command in the file is executed.

#### Scenario: No commands after EXIT in a file
- **GIVEN** a file containing a valid `USER CREATE`, then `EXIT`, then another valid `USER CREATE`
- **WHEN** the application runs that file
- **THEN** only the first user is created
