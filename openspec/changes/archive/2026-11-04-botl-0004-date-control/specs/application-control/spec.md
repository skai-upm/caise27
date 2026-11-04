# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The system SHALL accept the command `HELP` (without parameters) and print a first line starting with `OK` followed by one line per available command with its basic syntax. The list SHALL include at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `HELP` and `EXIT`.

#### Scenario: Help content
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `HELP` and `EXIT`, and no data changes

#### Scenario: Help with parameters
- **WHEN** `HELP;extra` is processed
- **THEN** the result starts with `ERROR` and processing continues
