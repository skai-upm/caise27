# command-processing Specification

## Purpose

Defines how the application receives text commands with an API-like format, splits them into an operation and parameters, and reports malformed commands without terminating.

## Requirements

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

### Requirement: Command syntax
A command SHALL consist of fields separated by `;`. The first field SHALL be the operation name (for example `USER CREATE`) and the remaining fields its parameters, interpreted positionally.

#### Scenario: Operation and parameters
- **WHEN** the command `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the operation `USER DELETE` is executed with parameters `ana@correo.com` and `Clave1!`

### Requirement: Malformed commands are reported
The application SHALL respond with a line starting with `ERROR` when a command is unknown or has fewer or more parameters than its syntax requires, and SHALL continue accepting commands afterwards.

#### Scenario: Unknown command
- **WHEN** the command `FOO BAR;1` is processed
- **THEN** the application prints an `ERROR` line stating the command is unknown and keeps running

#### Scenario: Insufficient parameters
- **WHEN** the command `USER CREATE;Ana;Garcia Lopez` is processed
- **THEN** the application prints an `ERROR` line indicating the expected syntax, no user is created, and the application keeps running

### Requirement: Interactive mode
When started without a command file argument (no arguments, or only `--fly`), the application SHALL read commands from standard input and keep waiting for new commands until it processes `EXIT` or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application is started without arguments and the user types `USER CREATE;...`, `HELP` and `EXIT`
- **THEN** each command is executed in order and the application stops asking for commands after `EXIT`

### Requirement: File mode
When started with a command file argument, the application SHALL interpret the first argument that is not `--fly` as the path of a command file, read it line by line and execute each non-blank line in order with the same command processing used in interactive mode.

#### Scenario: Commands from a file
- **WHEN** the application is started with `input_basico.txt` containing two `USER CREATE` lines, a `USER UPDATE` line and `EXIT`
- **THEN** the two users are created, the update is applied, and the application terminates

#### Scenario: Same result in both modes
- **WHEN** the same command is supplied once through the terminal and once through a file
- **THEN** it produces the same response and the same effect

#### Scenario: File after --fly
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** `input_fase2.txt` is used as the command file

### Requirement: Unreadable command file
If the command file does not exist, cannot be opened or cannot be read, the application SHALL print an `ERROR` line describing the problem and terminate without showing an unhandled Java exception.

#### Scenario: Missing file
- **WHEN** the application is started with the path of a file that does not exist
- **THEN** it prints an `ERROR` line naming the file and terminates normally

### Requirement: HELP command
The `HELP` command SHALL print every registered command with its basic usage syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP`, `HELP EXTENDED` and `EXIT`.

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

### Requirement: HELP EXTENDED command
The `HELP EXTENDED` command SHALL print, for every registered command, its syntax followed by a brief description of its behavior. Both `HELP` and `HELP EXTENDED` SHALL be produced from the same registry of commands.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command listed by `HELP` appears with its syntax and a description

#### Scenario: Unknown help mode
- **WHEN** `HELP VERBOSE` is processed
- **THEN** an `ERROR` line lists the available help modes

### Requirement: Injected help formats
The component that builds help text SHALL receive the formatting behavior for each help mode through dependency injection during application initialization, so that a new help format can be added by registering a new formatter without changing general command processing.

#### Scenario: New format registered
- **WHEN** an additional formatter is registered for a new help mode at startup
- **THEN** `HELP <mode>` uses it without changes to the command processor

### Requirement: Complete help
Every command accepted by the application, from all deliveries, SHALL be listed with its syntax by `HELP` and with its description by `HELP EXTENDED`, and every command they list SHALL be accepted by the application.

#### Scenario: Delivery 3 commands in help
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** both include `INVOICE CREATE`, `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE` and the typed `ATTRACTION CREATE` and `SPEED` entry syntax, together with every E1 and E2 command
