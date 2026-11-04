# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL print the commands currently available with their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `HELP` and `EXIT`, and the editable field names for `USER UPDATE`. It SHALL NOT change any data.

#### Scenario: Help content
- **WHEN** `HELP` is processed
- **THEN** the output lists the six command syntaxes above and the fields `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`

#### Scenario: Help with parameters
- **WHEN** `HELP;extra` is processed
- **THEN** an `ERROR` message is produced
