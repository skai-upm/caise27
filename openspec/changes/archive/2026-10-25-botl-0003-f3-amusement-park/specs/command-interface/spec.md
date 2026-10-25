# Spec Delta

## ADDED Requirements

### Requirement: HELP EXTENDED command
The `HELP EXTENDED` command SHALL display, for every registered command, its syntax and a brief description of its behaviour. `HELP` and `HELP EXTENDED` SHALL be generated from the same list of registered commands, and the help builder SHALL receive its output-formatting behaviour by dependency injection.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command syntax shown by `HELP` appears together with a description

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL display every currently available command with its basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`, the Delivery 2 commands `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION DELETE`, `ATTRACTION ENTER`, `ATTRACTION EXIT` and `ATTRACTION STATUS`, and the Delivery 3 commands `INVOICE CREATE`, `HELP EXTENDED`, `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE` and the typed and SPEED variants of `ATTRACTION CREATE` and `ATTRACTION ENTER`.

#### Scenario: Help lists commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`

#### Scenario: Help lists Delivery 2 commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of every system, group, ticket, group ticket and attraction command

#### Scenario: Help lists Delivery 3 commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of every invoice and pass command, `HELP EXTENDED`, `ATTRACTION CREATE` with a type and `ATTRACTION ENTER` with `SPEED`
