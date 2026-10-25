## MODIFIED Requirements

### Requirement: File mode
When started with at least one argument other than `--fly`, the application SHALL treat the first such argument as the path of a commands file, read it line by line and execute each line as a command in order, using the same command processing as interactive mode. When the only argument is `--fly`, the application SHALL run in interactive mode.

#### Scenario: Commands executed in order
- **WHEN** the application is started with a file containing two `USER CREATE` lines followed by a `USER UPDATE` line and `EXIT`
- **THEN** the users are created and then updated, in that order

#### Scenario: Same result as interactive
- **WHEN** the same command is supplied via file and via standard input
- **THEN** it produces the same result and output

#### Scenario: File after the fly flag
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** `input_fase2.txt` is executed as the commands file

### Requirement: HELP command
The `HELP` command SHALL display the available commands with their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;Id;IdTicket`, `ATTRACTION EXIT;Id;IdTicket`, `ATTRACTION STATUS;Id`, `HELP` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** each of the listed command syntaxes is printed
