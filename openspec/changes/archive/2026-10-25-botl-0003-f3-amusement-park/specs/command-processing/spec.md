# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL display every command currently available with its basic syntax: `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `SPEEDPASS CREATE;IdEntrada;Cantidad`, `SPEEDPASS DELETE;IdPase`, `SPECIALPASS CREATE;IdEntrada`, `SPECIALPASS DELETE;IdPase`, `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;IdAtraccion;IdEntrada`, `ATTRACTION ENTER;IdAtraccion;IdEntrada;SPEED`, `ATTRACTION EXIT;IdAtraccion;IdEntrada`, `ATTRACTION STATUS;IdAtraccion`, `INVOICE CREATE;USER;Correo`, `INVOICE CREATE;GROUP;IdGrupo`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Show help
- **WHEN** the line `HELP` is processed
- **THEN** the syntax of every user, system, group, ticket, group ticket, pass, attraction and invoice command, HELP, HELP EXTENDED and EXIT is displayed

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, for every registered command, its syntax and a brief description of its behavior.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command shown by `HELP` appears with its syntax followed by a description

### Requirement: Help built from registered commands
Both help variants SHALL be generated from the registry of commands the application actually registered, and the component that builds the help text SHALL receive its presentation format by dependency injection, so new formats can be added without changing command processing.

#### Scenario: New command appears automatically
- **WHEN** a command is registered at startup
- **THEN** it appears in both `HELP` and `HELP EXTENDED` without changing the help components
