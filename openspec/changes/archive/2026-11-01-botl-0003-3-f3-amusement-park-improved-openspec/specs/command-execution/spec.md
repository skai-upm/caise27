# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
`HELP` SHALL display every registered command with its basic syntax, consistent with the implemented operations, generated from the command registry: the E1 and E2 commands (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE/ADD/REMOVE/DELETE`, `TICKET CREATE/DELETE`, `GROUPTICKET CREATE/DELETE`, `ATTRACTION CREATE/ENTER/EXIT/STATUS`), the F3 commands (`INVOICE CREATE`, `SPEEDPASS CREATE/DELETE`, `SPECIALPASS CREATE/DELETE`), `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is executed
- **THEN** every listed command and its basic syntax is shown, including the F1 user commands, `HELP` and `EXIT`

#### Scenario: Help shows F3 syntax
- **WHEN** `HELP` is executed
- **THEN** it includes `INVOICE CREATE;USER;Correo`, `SPEEDPASS CREATE;IdTicket;Cantidad`, `ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;Tipo]` and `ATTRACTION ENTER;IdAtraccion;IdTicket[;SPEED]`

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, for every registered command, its syntax and a brief description of its behavior, using the same command registry as `HELP`.

#### Scenario: Syntax plus description
- **WHEN** `HELP EXTENDED` is executed
- **THEN** each registered command's syntax is shown together with a description

### Requirement: Injected help formatting
The component that builds help text SHALL receive the output-formatting behavior through dependency injection at startup, so new help formats can be added without changing general command processing.

#### Scenario: Alternative formatter
- **GIVEN** a different help formatter injected at startup
- **WHEN** help is requested
- **THEN** the output uses that formatter and command processing code is unchanged
