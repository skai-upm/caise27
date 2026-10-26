# command-interface Specification

## Purpose
Defines how the application receives API-like text commands from the terminal, splits them into operation and parameters, and reports results and errors.

## Requirements

### Requirement: Command parsing with semicolon-separated parameters
The system SHALL interpret each input line as one command whose components are separated by `;`. The first component identifies the operation (for example `USER CREATE`) and the remaining components are its parameters. Parsing SHALL use `String.split` and no external library.

#### Scenario: Recognised command is executed
- **WHEN** the line `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is received
- **THEN** the system executes the user-creation operation with the seven parameters

### Requirement: Terminal input
When started without a command file argument, the system SHALL read commands line by line from standard input and process each line in order until it receives `EXIT` or the input ends.

#### Scenario: Several commands from the terminal
- **WHEN** two valid command lines are entered and standard input then ends
- **THEN** both commands are processed in order and the application terminates normally

#### Scenario: EXIT ends interactive mode
- **WHEN** the lines `USER CREATE;...`, `HELP` and `EXIT` are entered
- **THEN** the system processes the first two, stops asking for commands after `EXIT` and terminates

#### Scenario: Only options supplied
- **WHEN** the application is started with `--fly` as its only argument
- **THEN** it reads commands from standard input

### Requirement: Invalid commands do not terminate the application
The system SHALL report an error message and continue processing when it receives an unknown operation or a command with an incorrect number of parameters.

#### Scenario: Unknown operation
- **WHEN** the line `USER FLY;x` is received
- **THEN** the system reports an unknown-command error and keeps accepting commands

#### Scenario: Insufficient parameters
- **WHEN** the line `USER DELETE;ana@correo.com` is received
- **THEN** the system reports that the parameter count is wrong and no user is deleted

### Requirement: Operation results are reported
The system SHALL print a line starting with `OK` when an operation succeeds and a line starting with `ERROR` explaining the reason when it fails.

#### Scenario: Successful operation
- **WHEN** a valid `USER CREATE` command is processed
- **THEN** the output line starts with `OK`

#### Scenario: Failed operation
- **WHEN** a `USER CREATE` command with an invalid DNI is processed
- **THEN** the output line starts with `ERROR` and mentions the DNI

### Requirement: Command file input
When an execution argument that is not an option (does not start with `--`) is supplied, the system SHALL treat the first such argument as the path of a text file, read it line by line and execute each line as a command in the order it appears.

#### Scenario: Commands executed from a file
- **WHEN** the application is started with `input_basico.txt` containing two `USER CREATE` lines, a `USER UPDATE` line and `EXIT`
- **THEN** both users are created, the update is applied and the application terminates

#### Scenario: File after an option
- **WHEN** the application is started with `--fly input_basico.txt`
- **THEN** `input_basico.txt` is used as the command file

### Requirement: Shared command processing
Interactive and file modes SHALL use the same command-processing mechanism, so that the same command produces the same result regardless of its source.

#### Scenario: Same result from both sources
- **WHEN** the same `USER CREATE` command is executed once from standard input and once from a file on equivalent empty states
- **THEN** both executions print the same response and store the same user

### Requirement: HELP command
The system SHALL provide a `HELP` command that displays every currently available command with its basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`.

#### Scenario: Help lists commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`

#### Scenario: Help lists the system date command
- **WHEN** `HELP` is processed
- **THEN** the output contains `SYSTEM DATE;yyyy-MM-dd`

### Requirement: EXIT command
The system SHALL provide an `EXIT` command that terminates processing in a controlled manner; no command after `EXIT` SHALL be executed, in either mode.

#### Scenario: Commands after EXIT in a file are ignored
- **WHEN** a file contains `USER CREATE;...` (Ana), `EXIT`, `USER CREATE;...` (Pedro)
- **THEN** only Ana is created and processing stops at `EXIT`

### Requirement: Invalid command file
If the supplied file does not exist, cannot be opened or cannot be read, the system SHALL report the problem with an error message and terminate in a controlled manner without showing an unhandled Java exception.

#### Scenario: Nonexistent file
- **WHEN** the application is started with a path that does not exist
- **THEN** an `ERROR` message naming the file is printed and the application terminates normally

### Requirement: Blank lines are ignored
The system SHALL ignore empty or whitespace-only lines in both modes.

#### Scenario: File with blank lines
- **WHEN** a file contains a `USER CREATE` line, a blank line, `HELP` and `EXIT`
- **THEN** all non-blank commands are processed and no error is reported for the blank line

### Requirement: Unknown options are rejected
An argument starting with `--` that is not a supported option SHALL be reported as an error and the application SHALL terminate without processing commands.

#### Scenario: Unknown option
- **WHEN** the application is started with `--fast`
- **THEN** an `ERROR` naming the option is printed and no command is processed

### Requirement: HELP EXTENDED command
The system SHALL provide `HELP EXTENDED`, which displays every registered command with its syntax and a brief explanation of its behaviour. `HELP` SHALL keep displaying only the syntax of every registered command. Both SHALL list `HELP EXTENDED` itself.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each command's syntax appears followed by its description, e.g. `USER DELETE;Correo;Contraseña` with an explanation that it deletes a user after checking the password

#### Scenario: Concise help unchanged
- **WHEN** `HELP` is processed
- **THEN** it lists the syntax of every registered command without descriptions

### Requirement: Help built from registered commands
Both help modes SHALL be generated from a single catalog of registered commands (syntax and description), so that registering a command makes it appear in both.

#### Scenario: Newly registered command
- **WHEN** a command is added to the catalog
- **THEN** it appears in the output of `HELP` and of `HELP EXTENDED` without changing the help-generation code

### Requirement: Injected help presentation behaviour
The component that builds help text SHALL receive the presentation behaviour (format) it uses through dependency injection and SHALL NOT instantiate its dependencies internally; command processing SHALL receive the configured help generators instead of creating them, so new help formats can be added without changing command processing.

#### Scenario: Alternative format
- **WHEN** a help generator is created with a different format implementation
- **THEN** it produces the catalog in that format and the command processing code is unchanged
