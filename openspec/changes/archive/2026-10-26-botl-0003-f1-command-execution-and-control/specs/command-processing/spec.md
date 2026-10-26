# Spec Delta

## MODIFIED Requirements

### Requirement: Line-based command input
The application SHALL read commands line by line from its command source (standard input or a command file), treating each line as one command and printing the response of each processed command. Blank lines SHALL be ignored.

#### Scenario: Command typed in the terminal
- **WHEN** the user types `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the application processes the command and prints its result

#### Scenario: Blank line
- **WHEN** the user enters an empty line
- **THEN** nothing is executed and no error is reported

#### Scenario: Blank lines in a command file
- **WHEN** a command file contains empty lines between commands
- **THEN** the empty lines are skipped and the remaining commands are processed without errors

## ADDED Requirements

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands from standard input and keep waiting for new commands until it processes `EXIT` or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application is started without arguments and the user types `USER CREATE;...`, `HELP` and `EXIT`
- **THEN** each command is executed in order and the application stops asking for commands after `EXIT`

### Requirement: File mode
When started with at least one argument, the application SHALL interpret the first argument as the path of a command file, read it line by line and execute each non-blank line in order with the same command processing used in interactive mode.

#### Scenario: Commands from a file
- **WHEN** the application is started with `input_basico.txt` containing two `USER CREATE` lines, a `USER UPDATE` line and `EXIT`
- **THEN** the two users are created, the update is applied, and the application terminates

#### Scenario: Same result in both modes
- **WHEN** the same command is supplied once through the terminal and once through a file
- **THEN** it produces the same response and the same effect

### Requirement: Unreadable command file
If the command file does not exist, cannot be opened or cannot be read, the application SHALL print an `ERROR` line describing the problem and terminate without showing an unhandled Java exception.

#### Scenario: Missing file
- **WHEN** the application is started with the path of a file that does not exist
- **THEN** it prints an `ERROR` line naming the file and terminates normally

### Requirement: HELP command
The `HELP` command SHALL print every currently available command with its basic usage syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`.

#### Scenario: Show help
- **WHEN** `HELP` is processed
- **THEN** the syntax of every available command is printed

### Requirement: EXIT command
The `EXIT` command SHALL terminate command processing in a controlled manner; no command after `EXIT` SHALL be executed in either mode.

#### Scenario: Exit in file mode
- **WHEN** a command file contains `USER CREATE;...`, `EXIT` and another `USER CREATE;...`
- **THEN** only the first user is created and processing stops at `EXIT`

#### Scenario: Exit in interactive mode
- **WHEN** the user types `EXIT`
- **THEN** the application stops reading standard input and terminates
