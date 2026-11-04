# application-control Specification

## Purpose

Defines the general commands that are not tied to a domain entity: showing the available commands and ending the application.

## Requirements

### Requirement: HELP command
The system SHALL accept the command `HELP` (without parameters) and print a first line starting with `OK` followed by one line per available command with its basic syntax. The list SHALL include at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `SYSTEM DATE;yyyy-MM-dd`, `HELP` and `EXIT`.

#### Scenario: Help content
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `HELP` and `EXIT`, and no data changes

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
