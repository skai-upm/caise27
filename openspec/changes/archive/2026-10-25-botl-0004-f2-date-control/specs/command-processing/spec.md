# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
`HELP` SHALL display every command currently available with its basic syntax, one per line: `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `HELP` and `EXIT`. The list SHALL be extended whenever new commands are added.

#### Scenario: Help lists the commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of the three `USER` commands, `SYSTEM DATE`, `HELP` and `EXIT`
