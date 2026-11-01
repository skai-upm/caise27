# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL display the concise syntax of every registered command, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;IdGrupo;Nombre`, `GROUP ADD;IdGrupo;Correo`, `GROUP REMOVE;IdGrupo;Correo`, `GROUP DELETE;IdGrupo`, `TICKET CREATE;IdTicket;Correo;yyyy-MM-dd`, `TICKET DELETE;IdTicket`, `GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;IdTicket`, `ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;Tipo]`, `ATTRACTION ENTER;IdAtraccion;IdTicket[;SPEED]`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `INVOICE CREATE;USER;Correo`, `INVOICE CREATE;GROUP;IdGrupo`, `SPEEDPASS CREATE;IdTicket;Cantidad`, `SPEEDPASS DELETE;IdPase`, `SPECIALPASS CREATE;IdTicket`, `SPECIALPASS DELETE;IdPase`, `HELP`, `HELP EXTENDED`, and `EXIT`.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is executed
- **THEN** every command above and its syntax are shown

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, for every registered command, its syntax and a brief description of its behaviour. `HELP` and `HELP EXTENDED` SHALL be generated from the same command registry by a help generator whose output format is injected, so new formats can be added without changing command processing.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is executed
- **THEN** each command listed by `HELP` is shown with its syntax and a description

#### Scenario: Injected help format
- **GIVEN** a help generator built with an alternative formatter
- **WHEN** help is generated
- **THEN** the alternative format is used without changes to the command processor
