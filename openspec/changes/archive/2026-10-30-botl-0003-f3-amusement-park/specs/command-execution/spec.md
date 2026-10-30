# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
`HELP` SHALL display the available commands with their basic syntax, consistent with the operations actually implemented, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;IdGrupo;Nombre`, `GROUP ADD;IdGrupo;Correo`, `GROUP REMOVE;IdGrupo;Correo`, `GROUP DELETE;IdGrupo`, `TICKET CREATE;IdTicket;Correo;Fecha`, `TICKET DELETE;IdTicket`, `GROUPTICKET CREATE;IdTicket;IdGrupo;Fecha`, `GROUPTICKET DELETE;IdTicket`, `ATTRACTION CREATE;IdAtraccion;Nombre;Aforo[;Tipo]`, `ATTRACTION ENTER;IdAtraccion;IdTicket[;SPEED]`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `INVOICE CREATE;USER;Correo`, `INVOICE CREATE;GROUP;IdGrupo`, `SPEEDPASS CREATE;IdTicket;Cantidad`, `SPEEDPASS DELETE;IdPase`, `SPECIALPASS CREATE;IdTicket`, `SPECIALPASS DELETE;IdPase`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is executed
- **THEN** every command listed above is displayed with its syntax

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display every registered command with its syntax and a brief description of its behavior. `HELP` and `HELP EXTENDED` SHALL be generated from the same registry of commands.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is executed
- **THEN** each command shown by `HELP` is displayed together with a description

### Requirement: Injected help formatting
The component that builds the help text SHALL receive the formatting behavior for each help variant through dependency injection at startup, so new help formats can be added without changing command processing.

#### Scenario: Alternative formatter
- **GIVEN** a different help formatter injected at startup
- **WHEN** `HELP` is executed
- **THEN** the output is produced by the injected formatter without changes to the command processor
