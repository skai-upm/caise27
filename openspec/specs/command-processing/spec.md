# command-processing Specification

## Purpose

Defines how textual API-like commands are interpreted and how general commands (HELP, EXIT) and command errors behave.

## Requirements

### Requirement: Semicolon-separated command format
Each command SHALL be a single line whose components are separated by `;`. The first component is the operation (for example `USER CREATE`) and the remaining components are its parameters. Leading and trailing whitespace of the line SHALL be ignored.

#### Scenario: Command with parameters
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the operation `USER DELETE` is executed with parameters `ana@correo.com` and `Clave1!`

### Requirement: Invalid commands do not terminate the program
An unknown operation, a wrong number of parameters or any invalid data SHALL produce an error message starting with `ERROR` and SHALL NOT terminate the application or display an unhandled exception.

#### Scenario: Unknown command
- **WHEN** the line `FOO BAR;1` is processed
- **THEN** an `ERROR` message reports the unknown command and the application keeps accepting commands

#### Scenario: Insufficient parameters
- **WHEN** the line `USER CREATE;Ana;Garcia` is processed
- **THEN** an `ERROR` message reports the wrong number of parameters and no user is created

### Requirement: Successful commands report OK
A command that succeeds SHALL print a message starting with `OK`.

#### Scenario: Successful command output
- **WHEN** a valid `USER CREATE` command is processed
- **THEN** a message starting with `OK` is printed

### Requirement: HELP command
The `HELP` command SHALL display every command currently available with its basic syntax: `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;IdAtraccion;IdEntrada`, `ATTRACTION EXIT;IdAtraccion;IdEntrada`, `ATTRACTION STATUS;IdAtraccion`, `HELP` and `EXIT`.

#### Scenario: Show help
- **WHEN** the line `HELP` is processed
- **THEN** the syntax of every user, system, group, ticket, group ticket and attraction command, HELP and EXIT is displayed

### Requirement: EXIT command
The `EXIT` command SHALL terminate command processing in a controlled manner; no command after `EXIT` SHALL be executed.

#### Scenario: Exit stops processing
- **WHEN** the commands `EXIT` and then `USER CREATE;...` are supplied
- **THEN** processing stops at `EXIT` and the user is not created
