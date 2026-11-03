# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL display every command currently available together with its basic syntax, at least: `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP` and `EXIT`.

#### Scenario: Help content
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of all fifteen commands

#### Scenario: Help does not change data
- **GIVEN** existing users
- **WHEN** `HELP` is processed
- **THEN** no user or stored data changes
