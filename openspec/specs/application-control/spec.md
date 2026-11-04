# application-control Specification

## Purpose
Defines the general commands that inform the user about available operations and terminate the application in a controlled way.

## Requirements

### Requirement: HELP command
The `HELP` command SHALL print the syntax of every command registered in the application, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;IdTicket`, `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo`, `ATTRACTION ENTER;IdAtraccion;IdTicket`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `INVOICE CREATE;USER;Correo`, `INVOICE CREATE;GROUP;IdGrupo`, `SPEEDPASS CREATE;IdCompra;IdTicket;Cantidad`, `SPEEDPASS DELETE;IdPase`, `SPECIALPASS CREATE;IdPase;IdTicket`, `SPECIALPASS DELETE;IdPase`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP`, `HELP EXTENDED` and `EXIT`, and the editable field names for `USER UPDATE`. It SHALL NOT change any data.

#### Scenario: Help content
- **WHEN** `HELP` is processed
- **THEN** the output lists the twenty-seven command syntaxes above and the fields `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`

#### Scenario: Help with parameters
- **WHEN** `HELP;extra` is processed
- **THEN** an `ERROR` message is produced

### Requirement: EXIT command
The `EXIT` command SHALL print a confirmation message and stop command processing immediately in both interactive and file modes; no command after `EXIT` SHALL be executed.

#### Scenario: No commands after EXIT in a file
- **GIVEN** a command file with a valid `USER CREATE` for Ana, then `EXIT`, then a valid `USER CREATE` for Pedro
- **WHEN** the file is processed
- **THEN** Ana is created, Pedro is not, and no result is printed for the line after `EXIT`

#### Scenario: Interactive EXIT
- **GIVEN** interactive mode
- **WHEN** `EXIT` is entered while more input is still available
- **THEN** the application stops reading standard input and terminates normally

### Requirement: HELP EXTENDED command
The `HELP EXTENDED` command SHALL print, for every registered command, its syntax and a brief explanation of its behaviour, using the same registered command information as `HELP`. It SHALL NOT change any data.

#### Scenario: Extended help content
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command listed by `HELP` appears with its syntax followed by a non-empty description, and the `USER UPDATE` description names the editable fields

#### Scenario: Extended help with parameters
- **WHEN** `HELP EXTENDED;extra` is processed
- **THEN** an `ERROR` message is produced

### Requirement: Injected help generation behaviour
The component that builds help text SHALL receive the registered command information and the behaviour that formats its output through dependency injection, so new help formats can be added without changing command parsing or execution; it SHALL NOT instantiate its formatting behaviour itself.

#### Scenario: Alternative format
- **GIVEN** the help component is created with an alternative formatting behaviour
- **WHEN** help is generated
- **THEN** the output is produced by that behaviour and command processing is unchanged
