# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
`HELP` SHALL display the currently available commands with their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;IdGrupo;Nombre`, `GROUP ADD;IdGrupo;Correo`, `GROUP REMOVE;IdGrupo;Correo`, `GROUP DELETE;IdGrupo`, `TICKET CREATE;IdTicket;Correo;yyyy-MM-dd`, `TICKET DELETE;IdTicket`, `GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;IdTicket`, `ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;Tipo]`, `ATTRACTION ENTER;IdAtraccion;IdTicket[;SPEED]`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `INVOICE CREATE;USER;Correo`, `INVOICE CREATE;GROUP;IdGrupo`, `SPEEDPASS CREATE;IdTicket;Cantidad`, `SPEEDPASS DELETE;IdPase`, `SPECIALPASS CREATE;IdTicket`, `SPECIALPASS DELETE;IdPase`, `HELP`, `HELP EXTENDED` and `EXIT`. The list SHALL be produced from the registry of supported commands.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is executed
- **THEN** every command above is shown with its syntax

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, for every registered command, its syntax followed by a brief description of its behavior. Both help modes SHALL be generated from the same command registry by a help generator whose output-format behavior is supplied by dependency injection.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is executed
- **THEN** each command shown by `HELP` appears with a description

#### Scenario: Alternative formatter
- **WHEN** a different help formatter is injected into the help generator at startup
- **THEN** help output uses it without changes to command processing
