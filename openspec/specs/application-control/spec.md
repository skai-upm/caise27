# application-control Specification

## Purpose
Defines the general commands that are not tied to any domain entity: listing the available commands and terminating the application in a controlled way.

## Requirements

### Requirement: HELP command
The `HELP` command SHALL print a response whose first line starts with `OK:` followed by one line per available command showing its syntax; it SHALL include at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`.

#### Scenario: Help lists Delivery 1 commands
- **WHEN** `HELP` is processed
- **THEN** the response contains the syntax lines of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`, and no data changes

#### Scenario: Help with parameters
- **WHEN** `HELP;extra` is processed
- **THEN** an `ERROR:` response is printed

### Requirement: EXIT command
The `EXIT` command SHALL print `OK: Exiting` and stop command processing in either mode, so no subsequent command is executed.

#### Scenario: Commands after EXIT are not executed
- **GIVEN** a file with a valid `USER CREATE` for Ana, then `EXIT`, then a valid `USER CREATE` for Pedro
- **WHEN** the file is executed
- **THEN** Ana is created, Pedro is not, and the application terminates with exit status 0

#### Scenario: EXIT in interactive mode
- **WHEN** `EXIT` is entered in interactive mode while more input is still available
- **THEN** the application stops reading standard input and terminates with exit status 0

### Requirement: HELP EXTENDED command
The `HELP EXTENDED` command SHALL print a response whose first line starts with `OK:` followed, for every registered command, by its syntax and a brief description of its behaviour; parameters after it SHALL be rejected with an `ERROR:` response.

#### Scenario: Extended help lists syntax and descriptions
- **WHEN** `HELP EXTENDED` is processed
- **THEN** for each command listed by `HELP` the response shows the same syntax followed by a non-empty description, and no data changes

#### Scenario: Extended help with parameters
- **WHEN** `HELP EXTENDED;x` is processed
- **THEN** an `ERROR:` response is printed

### Requirement: Help built from registered commands
`HELP` and `HELP EXTENDED` SHALL both be generated from the single registry of commands supported by the application, so they list the same commands in the same order, including `HELP EXTENDED` itself.

#### Scenario: Same commands in both modes
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** the syntax lines shown by both are identical and in the same order

### Requirement: Injected help output behaviour
The component that builds help text SHALL receive the behaviour used to format its output (and the command registry) through dependency injection at initialization, and SHALL NOT be part of general command parsing and execution, so a new help format can be added by providing another formatting behaviour.

#### Scenario: Alternative behaviour configured
- **GIVEN** a help component initialized with an alternative formatting behaviour
- **WHEN** it generates the help
- **THEN** the output is the one produced by that behaviour, and the command-processing loop is unchanged
