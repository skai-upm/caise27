# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
`HELP` SHALL display every command currently registered in the application with its basic syntax, one per line, generated from the registry of available commands. The registry SHALL be extended whenever new commands are added.

#### Scenario: Help lists the commands
- **WHEN** `HELP` is processed
- **THEN** the output contains, one per line, `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP` and `EXIT`

#### Scenario: Help lists the attraction commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains `ATTRACTION CREATE;Id;Nombre;Aforo`, `ATTRACTION CREATE;Id;Nombre;Aforo;Tipo`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;IdAtraccion;IdEntrada`, `ATTRACTION EXIT;IdAtraccion;IdEntrada` and `ATTRACTION STATUS;IdAtraccion`

#### Scenario: Help lists the invoice commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains `INVOICE CREATE;USER;Correo` and `INVOICE CREATE;GROUP;IdGrupo`

#### Scenario: Help lists the help modes
- **WHEN** `HELP` is processed
- **THEN** the output also contains `HELP EXTENDED`

#### Scenario: Help lists the pass commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains `SPEEDPASS CREATE;IdEntrada;Cantidad`, `SPEEDPASS DELETE;IdPase`, `SPECIALPASS CREATE;IdEntrada` and `SPECIALPASS DELETE;IdEntrada`
