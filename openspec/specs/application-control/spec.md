# application-control Specification

## Purpose

Defines the general commands that are not tied to a domain entity: showing the available commands and ending the application.

## Requirements

### Requirement: HELP command
The system SHALL accept the command `HELP` (without parameters) and print a first line starting with `OK` followed by one line per available command with its basic syntax. The list SHALL include at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;Id`, `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION ENTER;IdAtraccion;IdTicket`, `ATTRACTION EXIT;IdAtraccion;IdTicket`, `ATTRACTION STATUS;IdAtraccion`, `INVOICE CREATE;USER;Correo`, `INVOICE CREATE;GROUP;IdGrupo`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Help content
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION STATUS`, `INVOICE CREATE;USER`, `INVOICE CREATE;GROUP`, `SYSTEM DATE`, `SYSTEM PRICE`, `HELP`, `HELP EXTENDED` and `EXIT`, and no data changes

#### Scenario: Help with parameters
- **WHEN** `HELP;extra` is processed
- **THEN** the result starts with `ERROR` and processing continues

### Requirement: EXIT command
The system SHALL accept the command `EXIT` (without parameters), print a result line starting with `OK`, and stop processing commands in either input mode. No command after `EXIT` SHALL be executed.

#### Scenario: Commands after EXIT in a file
- **WHEN** a command file contains a valid user creation, then `EXIT`, then another valid user creation
- **THEN** only the first user is created

#### Scenario: EXIT in interactive mode
- **WHEN** `EXIT` is entered on standard input while more lines are still available
- **THEN** the application terminates and the remaining lines are not processed

#### Scenario: EXIT with parameters
- **WHEN** `EXIT;now` is processed
- **THEN** the result starts with `ERROR` and processing continues

### Requirement: HELP EXTENDED command
The system SHALL accept the command `HELP EXTENDED` (without parameters) and print a first line starting with `OK` followed, for every available command, by its syntax and a brief description of its behaviour. It SHALL list the same commands as `HELP`, in the same order.

#### Scenario: Extended help content
- **WHEN** `HELP EXTENDED` is processed
- **THEN** for each command listed by `HELP` the output contains its syntax followed by a non-empty description line, and no data changes

#### Scenario: Extended help with parameters
- **WHEN** `HELP EXTENDED;all` is processed
- **THEN** the result starts with `ERROR` and processing continues

### Requirement: Help built from the registered commands
Both help modes SHALL be generated from one registry of the commands the application supports, holding for each command its operation name, syntax and description. Every operation the command processor accepts SHALL have at least one registry entry (one per syntax form, as for the two `INVOICE CREATE` forms), and every registry entry SHALL be an operation the processor accepts.

#### Scenario: Registry and processor agree
- **WHEN** the registered operations are compared with the operations the command processor dispatches
- **THEN** both sets are identical

### Requirement: Injected help formatting behaviour
The component that builds help text SHALL receive the formatting behaviour for each help mode, and the command registry, from outside through dependency injection, and SHALL NOT create them itself. Command processing SHALL depend only on that component, so that a new help format can be added by supplying another formatting behaviour without changing command processing.

#### Scenario: Alternative formatting behaviour
- **GIVEN** the help component is created with a different formatting behaviour for the concise mode
- **WHEN** `HELP` is processed by a command processor using that component
- **THEN** the output is produced by the supplied behaviour and the command processor code is unchanged
