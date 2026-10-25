# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL display every registered command with its basic syntax (optional parameters in brackets), generated from the registered commands. It SHALL include at least the E1/E2 commands plus `INVOICE CREATE;USER;Correo`, `INVOICE CREATE;GROUP;IdGrupo`, `SPEEDPASS CREATE;IdEntrada;Cantidad`, `SPEEDPASS DELETE;IdSpeedPass`, `SPECIALPASS CREATE;IdEntrada`, `SPECIALPASS DELETE;IdEntrada`, `ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;Tipo]`, `ATTRACTION ENTER;IdAtraccion;IdEntrada[;SPEED]` and `HELP EXTENDED`.

#### Scenario: Display help
- **WHEN** the `HELP` command is processed
- **THEN** the syntax of every available command is printed

## ADDED Requirements

### Requirement: HELP EXTENDED command
The `HELP EXTENDED` command SHALL display, for every registered command, its syntax and a brief description of its behavior.

#### Scenario: Display extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each registered command is printed with its syntax followed by a description

### Requirement: Pluggable help formatting
Both help modes SHALL be produced from the same registered-command information by a help generator that receives its formatting behavior through dependency injection, so new help formats can be added without changing command processing.

#### Scenario: New command appears in both helps
- **WHEN** a command is registered
- **THEN** it appears in both `HELP` and `HELP EXTENDED` without changes to the help generator

### Requirement: Optional parameters
Commands MAY declare optional trailing parameters; a command SHALL be accepted when the number of parameters is between its minimum and maximum, and rejected with an error showing its syntax otherwise.

#### Scenario: Optional parameter omitted
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** it is accepted although the type parameter is omitted

#### Scenario: Too many parameters
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED;X` is processed
- **THEN** an `ERROR` result showing the command syntax is printed
