# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
`HELP` SHALL display every command currently available with its basic syntax, one per line: `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP` and `EXIT`. The list SHALL be extended whenever new commands are added.

#### Scenario: Help lists the commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of the three `USER` commands, the four `GROUP` commands, the two `TICKET` commands, `SYSTEM DATE`, `SYSTEM PRICE`, `HELP` and `EXIT`
