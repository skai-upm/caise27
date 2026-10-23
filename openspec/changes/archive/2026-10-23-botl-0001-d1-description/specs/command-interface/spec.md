# Spec Delta

## Purpose

Defines the text command language of the application: how `;`-separated commands are interpreted and dispatched, the general `HELP` and `EXIT` commands, and how invalid input is reported.

## ADDED Requirements

### Requirement: Semicolon-separated command syntax
Each command SHALL be a single line whose elements are separated by `;`. The first element is the command keyword (for example `USER CREATE`), matched ignoring case and surrounding whitespace; the remaining elements are its arguments, each trimmed of surrounding whitespace. Interpretation SHALL use basic String operations (including `split`) only, without regular-expression validation or external libraries.

#### Scenario: Keyword with arguments
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is dispatched to the user deletion operation with arguments `ana@correo.com` and `Clave1!`

#### Scenario: Keyword case is ignored
- **WHEN** the line `help` is processed
- **THEN** it behaves exactly as `HELP`

### Requirement: Supported commands
The application SHALL recognise the commands `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`.

#### Scenario: Unknown command
- **WHEN** the line `USER LIST` is processed
- **THEN** an error message stating that the command is unknown and suggesting `HELP` is printed and the application keeps running

### Requirement: Malformed commands never terminate the application
Any command with a wrong number of arguments, invalid values, or otherwise unprocessable content SHALL produce an error message (prefixed with `ERROR`) and processing SHALL continue with the next command.

#### Scenario: Wrong argument count
- **WHEN** the line `USER CREATE;Ana;Garcia` is processed
- **THEN** an error describing the expected syntax is printed and the next command is still processed

### Requirement: HELP command
The `HELP` command SHALL print the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>` (listing the modifiable fields), `HELP` and `EXIT`.

#### Scenario: Show help
- **WHEN** `HELP` is processed
- **THEN** the syntax of all five commands is printed

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner; in file mode no line after `EXIT` SHALL be processed.

#### Scenario: Lines after EXIT are ignored
- **WHEN** a command file contains `USER CREATE;...` (valid), `EXIT`, and then another valid `USER CREATE;...`
- **THEN** only the first user is created and the application terminates after `EXIT`
