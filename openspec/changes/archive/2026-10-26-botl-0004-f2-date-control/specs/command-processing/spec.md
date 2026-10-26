# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL print every command currently available with its basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `HELP` and `EXIT`.

#### Scenario: Help listing
- **WHEN** `HELP` is received
- **THEN** the output contains the syntax of USER CREATE, USER DELETE, USER UPDATE, SYSTEM DATE, HELP and EXIT

#### Scenario: HELP with parameters
- **WHEN** `HELP;extra` is received
- **THEN** an error about the number of parameters is printed and the application keeps running
