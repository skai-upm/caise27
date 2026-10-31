# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive mode
When launched without arguments, or with `--fly` as its only argument, the application SHALL read commands from standard input, one per line, processing each before reading the next, until `EXIT` is processed or input ends.

#### Scenario: Interactive session
- **WHEN** the application runs without arguments and receives `USER CREATE;...` (valid), `HELP` and `EXIT` on standard input
- **THEN** the user is created, help is shown, and the application ends after `EXIT`

#### Scenario: End of input
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates normally

#### Scenario: Interactive fly session
- **WHEN** the application runs with the single argument `--fly` and receives commands on standard input
- **THEN** the commands are processed interactively against temporary storage

### Requirement: File mode
When launched with a first argument other than `--fly`, the application SHALL treat it as a path to a text file; when the first argument is `--fly`, the second argument, if present, SHALL be that path. The application SHALL read the file line by line and execute each non-blank line as a command in file order. Blank lines SHALL be ignored.

#### Scenario: Ordered file processing with blank lines
- **WHEN** the file contains a valid `USER CREATE`, a blank line, `HELP` and `EXIT`
- **THEN** the user is created, help is shown, and the blank line causes no error

#### Scenario: File after --fly
- **WHEN** the application is launched with `--fly input_fase2.txt`
- **THEN** the commands of `input_fase2.txt` are executed in order

### Requirement: HELP command
`HELP` SHALL display the currently available commands with their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;IdGrupo;Nombre`, `GROUP ADD;IdGrupo;Correo`, `GROUP REMOVE;IdGrupo;Correo`, `GROUP DELETE;IdGrupo`, `TICKET CREATE;IdTicket;Correo;yyyy-MM-dd`, `TICKET DELETE;IdTicket`, `GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;IdTicket`, `ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad`, `ATTRACTION ENTER;IdAtraccion;IdTicket`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `HELP` and `EXIT`.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is executed
- **THEN** every command above is shown with its syntax
