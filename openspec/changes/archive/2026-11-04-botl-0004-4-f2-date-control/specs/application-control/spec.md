# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL print every command currently available with its basic syntax; at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `HELP` and `EXIT`, and SHALL change no data.

#### Scenario: Help lists all commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `HELP` and `EXIT`, and stored users and the current date are unchanged

#### Scenario: Help with parameters
- **WHEN** `HELP;extra` is processed
- **THEN** an error message is printed and processing continues
