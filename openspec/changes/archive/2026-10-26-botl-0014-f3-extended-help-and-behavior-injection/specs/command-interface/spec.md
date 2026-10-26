# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The command `HELP` SHALL display the commands currently available and their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP`, `HELP EXTENDED` and `EXIT`. The list SHALL be produced from the registered commands so that new commands appear in it, and SHALL show syntax only.

#### Scenario: Help lists all commands
- **WHEN** `HELP` is processed
- **THEN** the response lists the syntax of USER CREATE, USER DELETE, USER UPDATE, HELP, HELP EXTENDED and EXIT, one per line, without descriptions

#### Scenario: Help with parameters
- **WHEN** `HELP;x` is processed
- **THEN** an `ERROR` line about the wrong number of parameters is printed

## ADDED Requirements

### Requirement: HELP EXTENDED command
The command `HELP EXTENDED` SHALL display, for every registered command, its syntax followed by a brief explanation of its behaviour, starting with an `OK` line. It SHALL cover exactly the same commands as `HELP`.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each registered command's syntax appears followed by its description

#### Scenario: Same commands in both modes
- **WHEN** the commands listed by `HELP` and by `HELP EXTENDED` are compared
- **THEN** they are the same commands in the same order

### Requirement: Registered commands
Every supported command SHALL be registered once with its syntax and a non-empty description, and that registry SHALL be the single source used both to validate the number of parameters of a command and to generate the help.

#### Scenario: Every command described
- **WHEN** the command registry is inspected
- **THEN** every entry has a syntax and a non-empty description

### Requirement: Injected help behaviour
The class that constructs the help text SHALL receive, through dependency injection at application initialisation, the registered commands and the behaviour used to format them, and SHALL NOT instantiate those dependencies itself, so new help presentation formats can be added without changing command processing.

#### Scenario: New format without changing the processor
- **WHEN** a help generator is created with a different format behaviour
- **THEN** it produces help in that format for the same registered commands, with no change to the command processor
