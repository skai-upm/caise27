# Spec Delta

## MODIFIED Requirements

### Requirement: Terminal command input
The application SHALL read commands one per line from its command source (standard input in interactive mode, a file in file mode) and SHALL print one response per processed command; the `HELP` and `ATTRACTION STATUS` responses span several lines. It SHALL keep reading until it processes `EXIT` or the source ends.

#### Scenario: Several commands in sequence
- **WHEN** two valid `USER CREATE` lines are entered followed by end of input
- **THEN** two response lines are printed, one per command, and the application terminates normally

#### Scenario: Blank line
- **WHEN** an empty or whitespace-only line is entered
- **THEN** it is ignored and no response is printed for it

### Requirement: HELP command
`HELP` SHALL display every command currently available with its basic syntax, one per line. The list SHALL be extended whenever new commands are added.

#### Scenario: Help lists the commands
- **WHEN** `HELP` is processed
- **THEN** the output contains, one per line, `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP` and `EXIT`

#### Scenario: Help lists the attraction commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains `ATTRACTION CREATE;Id;Nombre;Aforo`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;IdAtraccion;IdEntrada`, `ATTRACTION EXIT;IdAtraccion;IdEntrada` and `ATTRACTION STATUS;IdAtraccion`
