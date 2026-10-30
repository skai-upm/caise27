# command-execution Specification

## Purpose

Lets the application receive text commands interactively from standard input or from a command file, processing both through one shared mechanism, with general HELP and EXIT commands.

## Requirements

### Requirement: Interactive mode
When launched without arguments, the application SHALL read commands from standard input, one command per line, processing each before reading the next, until `EXIT` is processed or the input ends.

#### Scenario: Interactive session
- **GIVEN** the application is launched without arguments
- **WHEN** the lines `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `HELP`, `EXIT` are entered
- **THEN** the user is created, the help is shown, and the application terminates normally

#### Scenario: End of input
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates normally

### Requirement: File mode
When launched with at least one argument, the application SHALL treat the first argument as the path of a text file, read it line by line, and process each non-blank line as a command in file order. Blank lines SHALL be ignored.

#### Scenario: Ordered processing with blank lines
- **GIVEN** a file with valid commands interleaved with blank lines
- **WHEN** the application runs in file mode
- **THEN** non-blank commands are executed in file order and blank lines cause no errors

### Requirement: Shared command processing
Interactive and file modes SHALL use the same command interpretation and execution logic; no command SHALL have separate implementations per mode. The same command SHALL yield the same observable result in both modes.

#### Scenario: Equivalent behavior across modes
- **GIVEN** equivalent initial persistent state
- **WHEN** the same command sequence is supplied interactively and through a file
- **THEN** each command produces the same output and resulting state

### Requirement: Recoverable invalid commands
Unknown commands, malformed commands or commands with insufficient parameters SHALL produce an error message and SHALL NOT terminate the application; subsequent commands SHALL continue to be processed.

#### Scenario: Unknown command in interactive mode
- **WHEN** `FOO BAR` is entered followed by `HELP`
- **THEN** an error is reported for `FOO BAR` and the help is then displayed

### Requirement: Controlled handling of invalid files
If the file argument does not exist, cannot be opened or cannot be read, the application SHALL report the problem with a message and terminate in a controlled manner, without displaying an unhandled Java exception.

#### Scenario: Nonexistent file
- **WHEN** the application is launched with a path that does not exist
- **THEN** an error message naming the file is printed and no stack trace is shown

### Requirement: HELP command
`HELP` SHALL display the available commands with their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`, consistent with the operations actually implemented.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is executed
- **THEN** the five commands and their syntax are displayed

### Requirement: EXIT command
`EXIT` SHALL terminate interactive execution in a controlled manner and SHALL stop file processing immediately, so that no command after `EXIT` is executed.

#### Scenario: No commands after EXIT in a file
- **GIVEN** a file containing a `USER CREATE` command, then `EXIT`, then another `USER CREATE` command
- **WHEN** the application runs in file mode
- **THEN** only the first user is created
