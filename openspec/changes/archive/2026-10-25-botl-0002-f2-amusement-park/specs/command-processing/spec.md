# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL display every command currently available with its basic syntax: `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;IdAtraccion;IdEntrada`, `ATTRACTION EXIT;IdAtraccion;IdEntrada`, `ATTRACTION STATUS;IdAtraccion`, `HELP` and `EXIT`.

#### Scenario: Show help
- **WHEN** the line `HELP` is processed
- **THEN** the syntax of every user, system, group, ticket, group ticket and attraction command, HELP and EXIT is displayed
