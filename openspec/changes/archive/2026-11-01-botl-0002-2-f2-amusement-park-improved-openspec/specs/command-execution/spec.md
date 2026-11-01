# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive mode
When launched without a command-file argument (with no arguments, or with only the `--fly` flag), the application SHALL read one command per line from standard input and process each, continuing until `EXIT` is processed or standard input ends.

#### Scenario: Commands read until EXIT
- **GIVEN** the application was started without arguments
- **WHEN** the lines `HELP` and then `EXIT` are entered
- **THEN** the help text is shown and the application then terminates normally

#### Scenario: End of input
- **GIVEN** the application was started without arguments
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates normally

#### Scenario: Interactive with --fly
- **GIVEN** the application was started with only `--fly`
- **WHEN** commands are entered on standard input
- **THEN** they are processed interactively

### Requirement: File mode
When launched with a command-file argument, the application SHALL treat the first argument other than `--fly` as the path of a command file, read it line by line, and process each nonblank line as a command in file order. Blank lines SHALL be ignored.

#### Scenario: Ordered file processing with blank lines
- **GIVEN** a file containing valid commands interleaved with blank lines
- **WHEN** the application runs with that file as first argument
- **THEN** the nonblank commands are executed in file order and the blank lines cause no error

#### Scenario: File with --fly
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** `input_fase2.txt` is processed as the command file

### Requirement: HELP command
`HELP` SHALL display every available command with its basic syntax, consistent with the implemented operations: `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;IdGrupo;Nombre`, `GROUP ADD;IdGrupo;Correo`, `GROUP REMOVE;IdGrupo;Correo`, `GROUP DELETE;IdGrupo`, `TICKET CREATE;IdTicket;Correo;yyyy-MM-dd`, `TICKET DELETE;IdTicket`, `GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;IdTicket`, `ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad`, `ATTRACTION ENTER;IdAtraccion;IdTicket`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `HELP` and `EXIT`.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is executed
- **THEN** every listed command and its basic syntax is shown, including the F1 user commands, `HELP` and `EXIT`
