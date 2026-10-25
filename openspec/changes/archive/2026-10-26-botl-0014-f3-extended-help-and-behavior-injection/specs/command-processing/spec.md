# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
`HELP` SHALL display every command currently registered in the application with its basic syntax, one per line, generated from the registry of available commands. The registry SHALL be extended whenever new commands are added.

#### Scenario: Help lists the commands
- **WHEN** `HELP` is processed
- **THEN** the output contains, one per line, `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP` and `EXIT`

#### Scenario: Help lists the attraction commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains `ATTRACTION CREATE;Id;Nombre;Aforo`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;IdAtraccion;IdEntrada`, `ATTRACTION EXIT;IdAtraccion;IdEntrada` and `ATTRACTION STATUS;IdAtraccion`

#### Scenario: Help lists the invoice commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains `INVOICE CREATE;USER;Correo` and `INVOICE CREATE;GROUP;IdGrupo`

#### Scenario: Help lists the help modes
- **WHEN** `HELP` is processed
- **THEN** the output also contains `HELP EXTENDED`

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, for every registered command, its syntax followed on the next line by a brief explanation of its behavior, indented by four spaces. It uses the same registry of commands as `HELP`, so both list exactly the same commands in the same order.

#### Scenario: Syntax and description
- **WHEN** `HELP EXTENDED` is processed
- **THEN** the output contains the line `USER DELETE;Correo;Contraseña` followed by an indented line describing that it deletes the user after checking email and password

#### Scenario: Same commands as HELP
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** every syntax line of `HELP` appears in `HELP EXTENDED` in the same order

### Requirement: Injected help format
The component that builds help texts SHALL receive the formatting behavior through dependency injection at startup and SHALL NOT instantiate its formats internally. Help modes SHALL be registered by command name, so a new presentation format can be added by configuration without changing general command processing.

#### Scenario: Alternative format
- **WHEN** a help generator is configured with a different format implementation and registered under a new help command name
- **THEN** that command produces the new presentation using the same registered commands, without changes to the command processor
