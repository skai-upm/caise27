# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL print every command currently available with its basic syntax; at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION ENTER;IdAtraccion;IdTicket`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP` and `EXIT`, and SHALL change no data.

#### Scenario: Help lists all commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of every user, group, ticket, group ticket, attraction and system command plus `HELP` and `EXIT`, and stored data, the current date and the price are unchanged

#### Scenario: Help with parameters
- **WHEN** `HELP;extra` is processed
- **THEN** an error message is printed and processing continues
