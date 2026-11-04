# application-control Specification

## Purpose
Defines the general commands that inform the user about available operations and terminate the application in a controlled way.

## Requirements

### Requirement: HELP command
The `HELP` command SHALL print the commands currently available with their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id`, `GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd`, `GROUPTICKET DELETE;IdTicket`, `SYSTEM DATE;yyyy-MM-dd`, `SYSTEM PRICE;Precio`, `HELP` and `EXIT`, and the editable field names for `USER UPDATE`. It SHALL NOT change any data.

#### Scenario: Help content
- **WHEN** `HELP` is processed
- **THEN** the output lists the fifteen command syntaxes above and the fields `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`

#### Scenario: Help with parameters
- **WHEN** `HELP;extra` is processed
- **THEN** an `ERROR` message is produced

### Requirement: EXIT command
The `EXIT` command SHALL print a confirmation message and stop command processing immediately in both interactive and file modes; no command after `EXIT` SHALL be executed.

#### Scenario: No commands after EXIT in a file
- **GIVEN** a command file with a valid `USER CREATE` for Ana, then `EXIT`, then a valid `USER CREATE` for Pedro
- **WHEN** the file is processed
- **THEN** Ana is created, Pedro is not, and no result is printed for the line after `EXIT`

#### Scenario: Interactive EXIT
- **GIVEN** interactive mode
- **WHEN** `EXIT` is entered while more input is still available
- **THEN** the application stops reading standard input and terminates normally
