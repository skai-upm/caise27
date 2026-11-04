# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The system SHALL accept the command `HELP` (without parameters) and print a first line starting with `OK` followed by one line per available command with its basic syntax. The list SHALL include at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION ENTER;IdAtraccion;IdTicket`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP` and `EXIT`.

#### Scenario: Help content
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION STATUS`, `SYSTEM DATE`, `SYSTEM PRICE`, `HELP` and `EXIT`, and no data changes

#### Scenario: Help with parameters
- **WHEN** `HELP;extra` is processed
- **THEN** the result starts with `ERROR` and processing continues
