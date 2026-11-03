# application-control Specification

## Purpose

Provides general commands to discover the available operations and to end a session in a controlled way, independent of the input source.

## Requirements

### Requirement: HELP command
The `HELP` command SHALL display every command currently available together with its basic syntax, at least: `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `SPEEDPASS CREATE;IdCompra;IdTicket;Cantidad`, `SPEEDPASS DELETE;IdPase`, `SPECIALPASS CREATE;IdPase;IdTicket`, `SPECIALPASS DELETE;IdPase`, `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION ENTER;IdAtraccion;IdTicket`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `INVOICE CREATE;USER;<correo>`, `INVOICE CREATE;GROUP;<identificadorGrupo>`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Help content
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of all twenty-six commands

#### Scenario: Help does not change data
- **GIVEN** existing users
- **WHEN** `HELP` is processed
- **THEN** no user or stored data changes

### Requirement: EXIT command
The `EXIT` command SHALL end command processing in a controlled manner in both input modes; no command after `EXIT` SHALL be executed.

#### Scenario: Commands after EXIT in a file are ignored
- **GIVEN** a file with a valid `USER CREATE` for Ana, then `EXIT`, then a valid `USER CREATE` for Pedro
- **WHEN** it is processed
- **THEN** Ana is created and Pedro is not

#### Scenario: EXIT stops interactive mode
- **GIVEN** interactive mode
- **WHEN** `EXIT` is entered followed by more lines
- **THEN** the application stops reading and the following lines are not processed
