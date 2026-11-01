# Spec Delta

## Purpose

Lets the application receive commands interactively from standard input or from a command file, processing both through one shared mechanism, with general `HELP` and `EXIT` commands and controlled error handling.

## ADDED Requirements

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands from standard input, one command per line, processing each line before reading the next, until it processes `EXIT` or standard input ends.

#### Scenario: Interactive session
- **GIVEN** the application is started without arguments
- **WHEN** the lines `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `HELP`, and `EXIT` are entered
- **THEN** each command is processed in order and the application ends after `EXIT`

#### Scenario: End of standard input
- **GIVEN** the application is started without arguments
- **WHEN** standard input ends without an `EXIT` command
- **THEN** the application ends in a controlled manner

### Requirement: File mode
When started with at least one argument, the application SHALL treat the first argument as the path of a text file of commands, read it line by line, and process each nonblank line as a command in file order. Blank or whitespace-only lines SHALL be ignored.

#### Scenario: Ordered file processing
- **GIVEN** a file with valid commands and blank lines
- **WHEN** file mode executes
- **THEN** the nonblank commands are executed in file order and blank lines cause no error

### Requirement: Shared command processing
Interactive mode and file mode SHALL use the same command interpretation and execution logic, so the same command produces the same result from either source.

#### Scenario: Equal behaviour across input modes
- **GIVEN** the same valid command sequence and equivalent initial persistent state
- **WHEN** the sequence is supplied interactively or through a command file
- **THEN** the observable results of each command are equivalent

### Requirement: Recoverable invalid commands
An unknown command, a malformed command, or a command with insufficient parameters SHALL produce an error message and SHALL NOT terminate the application; processing SHALL continue with the next command.

#### Scenario: Invalid input is recoverable
- **GIVEN** an active interactive session
- **WHEN** malformed or unknown command text is entered
- **THEN** an error message is reported and the application remains able to process subsequent input

### Requirement: Controlled handling of unusable command files
If the command file does not exist, cannot be opened, or cannot be read, the application SHALL report the problem with a message and terminate in a controlled manner, without displaying an unhandled Java exception.

#### Scenario: Nonexistent command file
- **WHEN** the application is started with the path of a file that does not exist
- **THEN** an error message naming the problem is reported, no stack trace is shown, and the application ends

### Requirement: HELP command
The `HELP` command SHALL display the commands currently available and their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP`, and `EXIT`.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is executed
- **THEN** the five commands above and their syntax are shown

### Requirement: EXIT command
The `EXIT` command SHALL end interactive execution in a controlled manner and SHALL stop command-file processing immediately, so that no command after `EXIT` is executed.

#### Scenario: No commands after EXIT in file mode
- **GIVEN** a file containing a valid `USER CREATE`, then `EXIT`, then another valid `USER CREATE`
- **WHEN** file mode runs
- **THEN** only the first creation is executed and the later creation is not executed

#### Scenario: EXIT ends interactive mode
- **GIVEN** an interactive session
- **WHEN** `EXIT` is entered
- **THEN** the application stops reading commands and ends
