# command-interface Specification

## Purpose

Defines how the application receives and executes text commands, both interactively from standard input and from an input file, including the general HELP and EXIT commands and controlled error handling.

## Requirements

### Requirement: Command parsing
Each command SHALL be a single line whose components are separated by `;`; the first component is the operation (for example `USER CREATE`) and the remaining components are its parameters. Parsing SHALL use `String.split` and no external libraries or regular-expression-based validation.

#### Scenario: Operation and parameters recognised
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is executed as the `USER DELETE` operation with parameters `ana@correo.com` and `Clave1!`

### Requirement: Invalid commands are handled
An unknown operation or a command with the wrong number of parameters SHALL be reported with an error message and SHALL NOT terminate the application.

#### Scenario: Unknown command
- **WHEN** the line `FOO BAR` is processed
- **THEN** an error message is shown and the application continues accepting commands

#### Scenario: Insufficient parameters
- **WHEN** the line `USER CREATE;Ana` is processed
- **THEN** an error message is shown, no user is created, and the application continues

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands line by line from standard input and process each one, until it receives `EXIT` or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application starts without arguments and receives `HELP` then `EXIT` on standard input
- **THEN** it shows the help text and terminates after `EXIT`

#### Scenario: End of input
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates normally

### Requirement: File mode
When started with at least one argument, the application SHALL treat the first argument as the path of a commands file, read it line by line and execute each line as a command in order, using the same command processing as interactive mode.

#### Scenario: Commands executed in order
- **WHEN** the application is started with a file containing two `USER CREATE` lines followed by a `USER UPDATE` line and `EXIT`
- **THEN** the users are created and then updated, in that order

#### Scenario: Same result as interactive
- **WHEN** the same command is supplied via file and via standard input
- **THEN** it produces the same result and output

### Requirement: Blank lines ignored
Blank or whitespace-only lines SHALL be ignored in both modes without producing an error.

#### Scenario: Blank line in file
- **WHEN** a commands file contains an empty line between two commands
- **THEN** both commands are executed and no error is reported for the empty line

### Requirement: Invalid input file
If the commands file does not exist, cannot be opened or cannot be read, the application SHALL report the problem and terminate in a controlled manner without showing an unhandled exception.

#### Scenario: Nonexistent file
- **WHEN** the application is started with a path to a file that does not exist
- **THEN** an error message naming the problem is shown and the application ends without a stack trace

### Requirement: HELP command
The `HELP` command SHALL display the available commands with their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** each of the listed command syntaxes is printed

### Requirement: EXIT command
The `EXIT` command SHALL terminate command processing in a controlled manner; in file mode no line after `EXIT` SHALL be executed.

#### Scenario: Commands after EXIT ignored
- **WHEN** a commands file contains `USER CREATE;...`, `EXIT`, and another `USER CREATE;...`
- **THEN** only the first user is created
