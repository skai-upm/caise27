# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive mode
When launched without a command-file argument (no arguments, or only `--fly`), the application SHALL read commands from standard input, one command per line, processing each before reading the next, until `EXIT` is processed or the input ends.

#### Scenario: Interactive session
- **GIVEN** the application is launched without arguments
- **WHEN** the lines `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `HELP`, `EXIT` are entered
- **THEN** the user is created, the help is shown, and the application terminates normally

#### Scenario: End of input
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates normally

#### Scenario: Interactive fly session
- **WHEN** the application is launched with only `--fly`
- **THEN** commands are read from standard input

### Requirement: File mode
When launched with a command-file argument, the application SHALL treat the first argument other than `--fly` as the path of a text file, read it line by line, and process each non-blank line as a command in file order. Blank lines SHALL be ignored.

#### Scenario: Ordered processing with blank lines
- **GIVEN** a file with valid commands interleaved with blank lines
- **WHEN** the application runs in file mode
- **THEN** non-blank commands are executed in file order and blank lines cause no errors

#### Scenario: File with fly flag
- **WHEN** the application is launched with `--fly input_fase2.txt`
- **THEN** `input_fase2.txt` is processed as the command file

### Requirement: HELP command
`HELP` SHALL display the available commands with their basic syntax, consistent with the operations actually implemented, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;IdGrupo;Nombre`, `GROUP ADD;IdGrupo;Correo`, `GROUP REMOVE;IdGrupo;Correo`, `GROUP DELETE;IdGrupo`, `TICKET CREATE;IdTicket;Correo;Fecha`, `TICKET DELETE;IdTicket`, `GROUPTICKET CREATE;IdTicket;IdGrupo;Fecha`, `GROUPTICKET DELETE;IdTicket`, `ATTRACTION CREATE;IdAtraccion;Nombre;Aforo`, `ATTRACTION ENTER;IdAtraccion;IdTicket`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `HELP` and `EXIT`.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is executed
- **THEN** every command listed above is displayed with its syntax
