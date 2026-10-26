# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL print the syntax of every registered command, taken from the catalog of registered commands, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;Id;IdTicket`, `ATTRACTION EXIT;Id;IdTicket`, `ATTRACTION STATUS;Id`, `INVOICE CREATE;USER;Correo`, `INVOICE CREATE;GROUP;IdGrupo`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Importe`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Help listing
- **WHEN** `HELP` is received
- **THEN** the output contains the syntax of every USER, GROUP, TICKET, GROUPTICKET, ATTRACTION, INVOICE and SYSTEM command listed above, HELP, HELP EXTENDED and EXIT

#### Scenario: HELP with parameters
- **WHEN** `HELP;extra` is received
- **THEN** an error about the number of parameters is printed and the application keeps running

## ADDED Requirements

### Requirement: Extended help
The command `HELP EXTENDED` SHALL print, for every registered command of the same catalog used by `HELP`, its syntax followed by a brief description of its behavior. An unknown help mode (e.g. `HELP SHORT`) SHALL be reported with an error.

#### Scenario: Extended listing
- **WHEN** `HELP EXTENDED` is received
- **THEN** every command syntax shown by `HELP` appears, each followed by a non-empty description, e.g. `TICKET DELETE;Id` is explained as deleting a ticket dated after the system date

#### Scenario: Unknown help mode
- **WHEN** `HELP SHORT` is received
- **THEN** an error indicating the help mode is unknown is printed

### Requirement: Injected help formatting
The component that builds help text SHALL receive the formatting behavior through dependency injection, and command processing SHALL receive the configured help generators instead of creating them; adding a new help format SHALL only require providing a new formatting behavior at startup.

#### Scenario: Alternative format
- **WHEN** a help generator is configured with a different formatting behavior
- **THEN** it renders the same registered commands in that format without changes to command processing
