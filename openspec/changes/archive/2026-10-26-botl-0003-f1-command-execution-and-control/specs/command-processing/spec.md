# Spec Delta

## MODIFIED Requirements

### Requirement: Terminal command input
When started without arguments, the application SHALL read commands from standard input, one command per line, and SHALL print a response for every non-empty command, continuing to wait for commands until it receives `EXIT` or the input ends.

#### Scenario: Command processed from terminal
- **WHEN** the user types `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the application prints a success message and waits for the next command

#### Scenario: Input ends
- **WHEN** the terminal input reaches end of stream
- **THEN** the application terminates normally

#### Scenario: EXIT ends interactive mode
- **WHEN** the user types `HELP` and then `EXIT`
- **THEN** the help is shown, the application stops asking for commands and terminates normally

### Requirement: Command format and splitting
A command SHALL consist of an operation optionally followed by parameters separated by `;`. The operation SHALL be either a single word (e.g. `HELP`, `EXIT`) or two words separated by a space (domain and action, e.g. `USER CREATE`). Commands SHALL be interpreted using `String.split`, without regular-expression-based matching or external libraries.

#### Scenario: Operation and parameters recognised
- **WHEN** the command `USER DELETE;ana@correo.com;Clave1!` is received
- **THEN** the operation is identified as `USER DELETE` with parameters `ana@correo.com` and `Clave1!`

#### Scenario: Single-word command recognised
- **WHEN** the command `HELP` is received
- **THEN** the operation is identified as `HELP` with no parameters

## ADDED Requirements

### Requirement: File command input
When started with at least one argument, the application SHALL treat the first argument as the path of a text file, read it line by line and execute each non-blank line as a command in file order, stopping at `EXIT` or at the end of the file.

#### Scenario: Commands executed in order
- **WHEN** the application is started with `input_basico.txt` containing two `USER CREATE` commands, a `USER UPDATE` of the first user's phone and `EXIT`
- **THEN** both users are created, the phone is updated, a response is printed for each command and the application terminates

#### Scenario: Blank lines in the file
- **WHEN** the file contains blank lines between commands
- **THEN** the blank lines are ignored and every command is processed without errors

### Requirement: Shared command processing
Interactive mode and file mode SHALL use the same command-interpretation and execution mechanism, so the same command produces the same result whichever source supplied it.

#### Scenario: Same result from both sources
- **WHEN** the same sequence of commands is supplied once through standard input and once through a file, each starting from the same stored users
- **THEN** the printed responses and the resulting stored users are identical

### Requirement: HELP command
The `HELP` command SHALL print every command currently available with its basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`.

#### Scenario: Help listing
- **WHEN** `HELP` is received
- **THEN** the output contains the syntax of USER CREATE, USER DELETE, USER UPDATE, HELP and EXIT

#### Scenario: HELP with parameters
- **WHEN** `HELP;extra` is received
- **THEN** an error about the number of parameters is printed and the application keeps running

### Requirement: EXIT command
The `EXIT` command SHALL end command processing in a controlled manner; no command after `EXIT` SHALL be executed, whether supplied interactively or from a file.

#### Scenario: Commands after EXIT in a file
- **WHEN** a file contains `USER CREATE` for Ana, `EXIT`, and `USER CREATE` for Pedro
- **THEN** only Ana is created and the application terminates after EXIT

### Requirement: Invalid command file
If the file given as first argument does not exist, cannot be opened or cannot be read, the application SHALL print an error message identifying the file and terminate without showing an unhandled Java exception.

#### Scenario: Nonexistent file
- **WHEN** the application is started with `no_existe.txt` and that file does not exist
- **THEN** an error message mentioning `no_existe.txt` is printed and the application terminates without a stack trace
