# Spec Delta

## MODIFIED Requirements

### Requirement: Terminal command input
The application SHALL read commands one per line from its command source (standard input in interactive mode, a file in file mode) and SHALL print one response per processed command; the `HELP` response spans several lines. It SHALL keep reading until it processes `EXIT` or the source ends.

#### Scenario: Several commands in sequence
- **WHEN** two valid `USER CREATE` lines are entered followed by end of input
- **THEN** two response lines are printed, one per command, and the application terminates normally

#### Scenario: Blank line
- **WHEN** an empty or whitespace-only line is entered
- **THEN** it is ignored and no response is printed for it

## ADDED Requirements

### Requirement: Execution mode selection
The application SHALL run in interactive mode, reading standard input, when started without arguments, and in file mode, reading the file whose path is the first argument, when started with at least one argument.

#### Scenario: No arguments
- **WHEN** the application is started without arguments and receives `HELP` and `EXIT` on standard input
- **THEN** it prints the help and terminates

#### Scenario: File argument
- **WHEN** the application is started with argument `input_basico.txt`
- **THEN** it processes the commands contained in that file instead of standard input

### Requirement: Command file execution
In file mode the application SHALL read the file line by line, interpret each non-blank line as a command and execute the commands in the order they appear, using exactly the same command processing as interactive mode, so the same command yields the same result from the terminal or from a file.

#### Scenario: Commands executed in order
- **WHEN** a file contains `USER CREATE` for Ana, `USER CREATE` for Pedro, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` and `EXIT`
- **THEN** both users are created, Ana's phone is updated, and the responses appear in that order

#### Scenario: Blank lines in a file
- **WHEN** a file contains blank lines between commands
- **THEN** the blank lines are ignored and the other commands are processed normally

### Requirement: HELP command
`HELP` SHALL display every command currently available with its basic syntax, one per line: `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`. The list SHALL be extended whenever new commands are added.

#### Scenario: Help lists the commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of the three `USER` commands, `HELP` and `EXIT`

### Requirement: EXIT command
`EXIT` SHALL terminate the application in a controlled manner after printing `OK EXIT`. No command after `EXIT` SHALL be read or executed, in either mode.

#### Scenario: Exit in interactive mode
- **WHEN** `HELP` and then `EXIT` are entered interactively
- **THEN** the application stops asking for commands after `EXIT`

#### Scenario: Commands after EXIT in a file
- **WHEN** a file contains `USER CREATE` for Ana, `EXIT`, and `USER CREATE` for Pedro
- **THEN** only Ana is created and processing stops at `EXIT`

### Requirement: Invalid command file
If the command file does not exist, cannot be opened or cannot be read, the application SHALL print `ERROR CANNOT READ FILE <path>` and terminate in a controlled manner, without displaying an unhandled Java exception.

#### Scenario: Missing file
- **WHEN** the application is started with argument `no_existe.txt` and that file does not exist
- **THEN** it prints `ERROR CANNOT READ FILE no_existe.txt` and terminates without a stack trace
