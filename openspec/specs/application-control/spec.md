# application-control Specification

## Purpose

Defines the general commands that are not tied to a domain entity: listing available commands with their syntax and stopping command processing in a controlled way.

## Requirements

### Requirement: HELP command
The `HELP` command SHALL print every command currently available with its basic syntax; at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP` and `EXIT`, and SHALL change no data.

#### Scenario: Help lists all commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `SYSTEM DATE`, `SYSTEM PRICE`, `HELP` and `EXIT`, and stored data, the current date and the price are unchanged

#### Scenario: Help with parameters
- **WHEN** `HELP;extra` is processed
- **THEN** an error message is printed and processing continues

### Requirement: EXIT command
The `EXIT` command SHALL stop command processing immediately in either input mode; no command after `EXIT` SHALL be executed, and the application SHALL terminate normally.

#### Scenario: Commands after EXIT are not executed
- **GIVEN** a command file with a valid `USER CREATE` for Ana, then `EXIT`, then a valid `USER CREATE` for Pedro
- **WHEN** the file is processed
- **THEN** Ana is created, Pedro is not, and the application terminates normally

#### Scenario: Interactive EXIT
- **GIVEN** interactive mode
- **WHEN** `EXIT` is entered
- **THEN** the application stops reading standard input and terminates normally
