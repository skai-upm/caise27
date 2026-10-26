# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL print every command currently available with its basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Importe`, `HELP` and `EXIT`.

#### Scenario: Help listing
- **WHEN** `HELP` is received
- **THEN** the output contains the syntax of USER CREATE, USER DELETE, USER UPDATE, GROUP CREATE, GROUP ADD, GROUP REMOVE, GROUP DELETE, TICKET CREATE, TICKET DELETE, SYSTEM DATE, SYSTEM PRICE, HELP and EXIT

#### Scenario: HELP with parameters
- **WHEN** `HELP;extra` is received
- **THEN** an error about the number of parameters is printed and the application keeps running
