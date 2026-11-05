# application-control Specification

## Purpose

Defines the general commands that are not tied to a domain entity: listing available commands with their syntax and stopping command processing in a controlled way.

## Requirements

### Requirement: HELP command
The `HELP` command SHALL print every command currently available with its basic syntax; at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `SPEEDPASS CREATE;IdCompra;IdTicket;Cantidad`, `SPEEDPASS DELETE;IdPase`, `SPECIALPASS CREATE;Id;IdTicket`, `SPECIALPASS DELETE;IdPase`, `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo`, `ATTRACTION ENTER;IdAtraccion;IdTicket`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `INVOICE CREATE;USER;Correo`, `INVOICE CREATE;GROUP;IdGrupo`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP`, `HELP EXTENDED` and `EXIT`, and SHALL change no data.

#### Scenario: Help lists all commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of every user, group, ticket, group ticket, pass, attraction, invoice and system command plus `HELP`, `HELP EXTENDED` and `EXIT`, without descriptions, and stored data, the current date and the price are unchanged

#### Scenario: Help with parameters
- **WHEN** `HELP;extra` is processed
- **THEN** an error message is printed and processing continues

### Requirement: EXIT command
The `EXIT` command SHALL stop command processing immediately in either input mode; no command after `EXIT` SHALL be executed, and the application SHALL terminate normally.

#### Scenario: Commands after EXIT are not executed
- **GIVEN** a command file with a valid `USER CREATE` for Ana, then `EXIT`, then a valid `USER CREATE` for Pedro
- **WHEN** the file is processed
- **THEN** Ana is created, Pedro is not, and the application terminates normally

#### Scenario: Interactive EXIT
- **GIVEN** interactive mode
- **WHEN** `EXIT` is entered
- **THEN** the application stops reading standard input and terminates normally

### Requirement: HELP EXTENDED command
The `HELP EXTENDED` command SHALL print, for every command listed by `HELP` and in the same order, its syntax and a brief explanation of its behavior, and SHALL change no data; parameters after it SHALL be rejected with a usage error.

#### Scenario: Syntax and description for every command
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every syntax shown by `HELP` appears, each followed by a non-empty explanation (for example `USER DELETE;Correo;Contraseña` with an explanation that it deletes a user after checking the password)

#### Scenario: Extended help with parameters
- **WHEN** `HELP EXTENDED;x` is processed
- **THEN** an error message is printed and processing continues

### Requirement: Help generated from registered commands with an injected format
Both help modes SHALL be produced from the same registry of available commands, by a help component that receives the formatting behavior it uses through injection when the application starts, so that a new help format can be supplied without changing command parsing or execution.

#### Scenario: Alternative format injected
- **GIVEN** a help component configured with a different formatting behavior
- **WHEN** help is requested through the command processor
- **THEN** the output follows the configured behavior and the command-processing code is unchanged

#### Scenario: Single source of command information
- **WHEN** a command is added to the registry
- **THEN** it appears in both `HELP` and `HELP EXTENDED`
