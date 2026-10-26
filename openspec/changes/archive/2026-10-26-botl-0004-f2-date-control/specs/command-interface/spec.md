# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The system SHALL provide a `HELP` command that displays every currently available command with its basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`.

#### Scenario: Help lists commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`

#### Scenario: Help lists the system date command
- **WHEN** `HELP` is processed
- **THEN** the output contains `SYSTEM DATE;yyyy-MM-dd`
