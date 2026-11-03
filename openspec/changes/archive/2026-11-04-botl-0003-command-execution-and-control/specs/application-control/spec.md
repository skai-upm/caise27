# Spec Delta

## Purpose

Defines the general commands that are not tied to any domain entity: listing the available commands and terminating the application in a controlled way.

## ADDED Requirements

### Requirement: HELP command
The `HELP` command SHALL print a response whose first line starts with `OK:` followed by one line per available command showing its syntax; it SHALL include at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`.

#### Scenario: Help lists Delivery 1 commands
- **WHEN** `HELP` is processed
- **THEN** the response contains the syntax lines of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`, and no data changes

#### Scenario: Help with parameters
- **WHEN** `HELP;extra` is processed
- **THEN** an `ERROR:` response is printed

### Requirement: EXIT command
The `EXIT` command SHALL print `OK: Exiting` and stop command processing in either mode, so no subsequent command is executed.

#### Scenario: Commands after EXIT are not executed
- **GIVEN** a file with a valid `USER CREATE` for Ana, then `EXIT`, then a valid `USER CREATE` for Pedro
- **WHEN** the file is executed
- **THEN** Ana is created, Pedro is not, and the application terminates with exit status 0

#### Scenario: EXIT in interactive mode
- **WHEN** `EXIT` is entered in interactive mode while more input is still available
- **THEN** the application stops reading standard input and terminates with exit status 0
