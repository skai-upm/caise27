# Spec Delta

## ADDED Requirements

### Requirement: HELP EXTENDED command
The system SHALL provide `HELP EXTENDED`, which displays every registered command with its syntax and a brief explanation of its behaviour. `HELP` SHALL keep displaying only the syntax of every registered command. Both SHALL list `HELP EXTENDED` itself.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each command's syntax appears followed by its description, e.g. `USER DELETE;Correo;Contraseña` with an explanation that it deletes a user after checking the password

#### Scenario: Concise help unchanged
- **WHEN** `HELP` is processed
- **THEN** it lists the syntax of every registered command without descriptions

### Requirement: Help built from registered commands
Both help modes SHALL be generated from a single catalog of registered commands (syntax and description), so that registering a command makes it appear in both.

#### Scenario: Newly registered command
- **WHEN** a command is added to the catalog
- **THEN** it appears in the output of `HELP` and of `HELP EXTENDED` without changing the help-generation code

### Requirement: Injected help presentation behaviour
The component that builds help text SHALL receive the presentation behaviour (format) it uses through dependency injection and SHALL NOT instantiate its dependencies internally; command processing SHALL receive the configured help generators instead of creating them, so new help formats can be added without changing command processing.

#### Scenario: Alternative format
- **WHEN** a help generator is created with a different format implementation
- **THEN** it produces the catalog in that format and the command processing code is unchanged
