# Spec Delta

## MODIFIED Requirements

### Requirement: File mode
When started with a non-option argument, the application SHALL interpret the first such argument as the path to a text file containing one command per line, and SHALL execute those commands in the order they appear. The `--fly` option MAY appear before the file path. Unknown options or more than one file path SHALL be reported as an error and the application SHALL terminate in a controlled manner.

#### Scenario: Commands executed in order
- **WHEN** the application is started with a file containing a `USER CREATE` line followed by a `USER UPDATE` line for the same user
- **THEN** the user is created first and then updated

#### Scenario: Fly option with a file
- **WHEN** the application is started with arguments `--fly input_fase2.txt`
- **THEN** the commands of `input_fase2.txt` are executed

#### Scenario: Invalid arguments
- **WHEN** the application is started with an unknown option such as `--foo` or with two file paths
- **THEN** an error message is printed and no command is executed

### Requirement: HELP command
The `HELP` command SHALL display the available commands with their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;IdGrupo;Nombre`, `GROUP ADD;IdGrupo;Correo`, `GROUP REMOVE;IdGrupo;Correo`, `GROUP DELETE;IdGrupo`, `TICKET CREATE;IdEntrada;Correo;Fecha`, `TICKET DELETE;IdEntrada`, `GROUPTICKET CREATE;IdEntrada;IdGrupo;Fecha`, `GROUPTICKET DELETE;IdEntrada`, `ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad`, `ATTRACTION DELETE;IdAtraccion`, `ATTRACTION ENTER;IdAtraccion;IdEntrada`, `ATTRACTION EXIT;IdAtraccion;IdEntrada`, `ATTRACTION STATUS;IdAtraccion`, `HELP` and `EXIT`.

#### Scenario: Display help
- **WHEN** the `HELP` command is processed
- **THEN** the syntax of every available command is printed
