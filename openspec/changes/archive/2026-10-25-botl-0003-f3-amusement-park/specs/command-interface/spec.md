## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL display the available commands with their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;Id;IdTicket`, `ATTRACTION EXIT;Id;IdTicket`, `ATTRACTION STATUS;Id`, `HELP` and `EXIT`. It SHALL also list `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo`, `ATTRACTION ENTER;Id;IdTicket;SPEED`, `INVOICE CREATE;USER;Correo`, `INVOICE CREATE;GROUP;IdGrupo`, `SPEEDPASS CREATE;IdTicket;Cantidad`, `SPEEDPASS DELETE;IdPase`, `SPECIALPASS CREATE;IdTicket`, `SPECIALPASS DELETE;IdPase` and `HELP EXTENDED`.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** each of the listed command syntaxes is printed

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display every command listed by `HELP` together with a brief description of its behaviour. Both help variants SHALL be generated from the same registry of commands, and the presentation format SHALL be supplied to the help generator by dependency injection so new formats can be added without changing command processing.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each command syntax is printed followed by its description

#### Scenario: Registry shared
- **WHEN** a command is added to the registry
- **THEN** it appears in both `HELP` and `HELP EXTENDED`
