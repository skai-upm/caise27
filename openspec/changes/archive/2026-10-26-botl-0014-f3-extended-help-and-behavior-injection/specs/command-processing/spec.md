# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL print every registered command with its basic usage syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Show help
- **WHEN** `HELP` is processed
- **THEN** the syntax of every available command is printed

## ADDED Requirements

### Requirement: HELP EXTENDED command
The `HELP EXTENDED` command SHALL print, for every registered command, its syntax followed by a brief description of its behavior. Both `HELP` and `HELP EXTENDED` SHALL be produced from the same registry of commands.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command listed by `HELP` appears with its syntax and a description

#### Scenario: Unknown help mode
- **WHEN** `HELP VERBOSE` is processed
- **THEN** an `ERROR` line lists the available help modes

### Requirement: Injected help formats
The component that builds help text SHALL receive the formatting behavior for each help mode through dependency injection during application initialization, so that a new help format can be added by registering a new formatter without changing general command processing.

#### Scenario: New format registered
- **WHEN** an additional formatter is registered for a new help mode at startup
- **THEN** `HELP <mode>` uses it without changes to the command processor
