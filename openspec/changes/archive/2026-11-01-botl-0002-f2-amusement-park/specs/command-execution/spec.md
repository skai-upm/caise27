# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive mode
When started without a command-file argument (no arguments, or only `--fly`), the application SHALL read commands from standard input, one command per line, processing each line before reading the next, until it processes `EXIT` or standard input ends.

#### Scenario: Interactive session
- **GIVEN** the application is started without arguments
- **WHEN** the lines `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `HELP`, and `EXIT` are entered
- **THEN** each command is processed in order and the application ends after `EXIT`

#### Scenario: End of standard input
- **GIVEN** the application is started without arguments
- **WHEN** standard input ends without an `EXIT` command
- **THEN** the application ends in a controlled manner

#### Scenario: Interactive fly session
- **GIVEN** the application is started with the single argument `--fly`
- **WHEN** commands are entered on standard input
- **THEN** they are processed interactively against fly storage

### Requirement: File mode
When started with a command-file argument, the application SHALL treat that argument as the path of a text file of commands, read it line by line, and process each nonblank line as a command in file order. The command-file argument SHALL be the first argument, or the second when the first is `--fly`. Blank or whitespace-only lines SHALL be ignored.

#### Scenario: Ordered file processing
- **GIVEN** a file with valid commands and blank lines
- **WHEN** file mode executes
- **THEN** the nonblank commands are executed in file order and blank lines cause no error

#### Scenario: File after fly flag
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** `input_fase2.txt` is processed as the command file

### Requirement: HELP command
The `HELP` command SHALL display the commands currently available and their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;IdGrupo;Nombre`, `GROUP ADD;IdGrupo;Correo`, `GROUP REMOVE;IdGrupo;Correo`, `GROUP DELETE;IdGrupo`, `TICKET CREATE;IdTicket;Correo;yyyy-MM-dd`, `TICKET DELETE;IdTicket`, `GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;IdTicket`, `ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad`, `ATTRACTION ENTER;IdAtraccion;IdTicket`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `HELP`, and `EXIT`.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is executed
- **THEN** every command above and its syntax are shown
