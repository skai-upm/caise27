# command-processing Specification

## Purpose

Defines how the application receives text commands with an API-like format, splits them into an operation and its parameters, dispatches them and reports malformed input without terminating.

## Requirements

### Requirement: Terminal command input
The application SHALL read commands one per line from its command source (standard input in interactive mode, a file in file mode) and SHALL print one response per processed command; some responses, such as `HELP`, `ATTRACTION STATUS` and invoices, span several lines. It SHALL keep reading until it processes `EXIT` or the source ends.

#### Scenario: Several commands in sequence
- **WHEN** two valid `USER CREATE` lines are entered followed by end of input
- **THEN** two response lines are printed, one per command, and the application terminates normally

#### Scenario: Blank line
- **WHEN** an empty or whitespace-only line is entered
- **THEN** it is ignored and no response is printed for it

### Requirement: Command syntax
A command SHALL be a text line whose components are separated by `;`, interpreted with `String.split` and without regular-expression matching or external libraries. The first component is the operation name (for example `USER CREATE`); the remaining components are its parameters. The operation name is matched ignoring letter case and surrounding spaces, and each parameter is trimmed.

#### Scenario: Operation and parameters are separated
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the operation `USER DELETE` is executed with parameters `ana@correo.com` and `Clave1!`

### Requirement: Response format
Successful operations SHALL respond with a line starting with `OK`, and rejected operations with a line starting with `ERROR` followed by the reason.

#### Scenario: Successful and failed responses
- **WHEN** a valid `USER CREATE` is processed and then an identical one
- **THEN** the first response starts with `OK` and the second starts with `ERROR`

### Requirement: Invalid commands do not terminate the application
An unknown operation, or a known operation with fewer or more parameters than its format defines, SHALL be rejected with an `ERROR` response, and the application SHALL continue processing subsequent commands. No unhandled exception SHALL reach the user.

#### Scenario: Unknown operation
- **WHEN** the line `USER FLY;x` is processed
- **THEN** the response is `ERROR UNKNOWN COMMAND` and the next command is still processed

#### Scenario: Insufficient parameters
- **WHEN** the line `USER CREATE;Ana;Garcia Lopez` is processed
- **THEN** the response is `ERROR WRONG NUMBER OF PARAMETERS` and no user is created

### Requirement: Execution mode selection
The application SHALL run in interactive mode, reading standard input, when started without arguments, and in file mode, reading the file whose path is the first argument, when started with at least one argument. The option `--fly` (temporary persistence) is not a file path: it is removed from the arguments before this selection, so `--fly` alone means interactive mode and `--fly <file>` means file mode.

#### Scenario: No arguments
- **WHEN** the application is started without arguments and receives `HELP` and `EXIT` on standard input
- **THEN** it prints the help and terminates

#### Scenario: File argument
- **WHEN** the application is started with argument `input_basico.txt`
- **THEN** it processes the commands contained in that file instead of standard input

#### Scenario: Fly option is not a file
- **WHEN** the application is started with the single argument `--fly`
- **THEN** it runs in interactive mode with temporary persistence

### Requirement: Command file execution
In file mode the application SHALL read the file line by line, interpret each non-blank line as a command and execute the commands in the order they appear, using exactly the same command processing as interactive mode, so the same command yields the same result from the terminal or from a file.

#### Scenario: Commands executed in order
- **WHEN** a file contains `USER CREATE` for Ana, `USER CREATE` for Pedro, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` and `EXIT`
- **THEN** both users are created, Ana's phone is updated, and the responses appear in that order

#### Scenario: Blank lines in a file
- **WHEN** a file contains blank lines between commands
- **THEN** the blank lines are ignored and the other commands are processed normally

### Requirement: HELP command
`HELP` SHALL display every command currently registered in the application with its basic syntax, one per line, generated from the registry of available commands. The registry SHALL be extended whenever new commands are added.

#### Scenario: Help lists the commands
- **WHEN** `HELP` is processed
- **THEN** the output contains, one per line, `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP` and `EXIT`

#### Scenario: Help lists the attraction commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains `ATTRACTION CREATE;Id;Nombre;Aforo`, `ATTRACTION CREATE;Id;Nombre;Aforo;Tipo`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;IdAtraccion;IdEntrada`, `ATTRACTION ENTER;IdAtraccion;IdEntrada;SPEED`, `ATTRACTION EXIT;IdAtraccion;IdEntrada` and `ATTRACTION STATUS;IdAtraccion`

#### Scenario: Help lists the invoice commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains `INVOICE CREATE;USER;Correo` and `INVOICE CREATE;GROUP;IdGrupo`

#### Scenario: Help lists the help modes
- **WHEN** `HELP` is processed
- **THEN** the output also contains `HELP EXTENDED`

#### Scenario: Help lists the pass commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains `SPEEDPASS CREATE;IdEntrada;Cantidad`, `SPEEDPASS DELETE;IdPase`, `SPECIALPASS CREATE;IdEntrada` and `SPECIALPASS DELETE;IdEntrada`

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

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, for every registered command, its syntax followed on the next line by a brief explanation of its behavior, indented by four spaces. It uses the same registry of commands as `HELP`, so both list exactly the same commands in the same order.

#### Scenario: Syntax and description
- **WHEN** `HELP EXTENDED` is processed
- **THEN** the output contains the line `USER DELETE;Correo;Contraseña` followed by an indented line describing that it deletes the user after checking email and password

#### Scenario: Same commands as HELP
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** every syntax line of `HELP` appears in `HELP EXTENDED` in the same order

### Requirement: Injected help format
The component that builds help texts SHALL receive the formatting behavior through dependency injection at startup and SHALL NOT instantiate its formats internally. Help modes SHALL be registered by command name, so a new presentation format can be added by configuration without changing general command processing.

#### Scenario: Alternative format
- **WHEN** a help generator is configured with a different format implementation and registered under a new help command name
- **THEN** that command produces the new presentation using the same registered commands, without changes to the command processor

### Requirement: Documented and dispatched commands match
Every command listed by `HELP` and `HELP EXTENDED`, including the Delivery 3 commands `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, the typed `ATTRACTION CREATE` and the `ATTRACTION ENTER` variants, SHALL be recognised by the command processor, and the processor SHALL not accept any command absent from the help.

#### Scenario: Every help entry is recognised
- **WHEN** each command of the help listing is processed with its documented number of parameters
- **THEN** none of them answers `ERROR UNKNOWN COMMAND`

#### Scenario: Delivery 3 commands in both help modes
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** both list the SpeedPass, Special Pass and typed attraction commands
