# Spec Delta

## ADDED Requirements

### Requirement: HELP EXTENDED shows syntax and purpose
The system SHALL respond to `HELP EXTENDED` by displaying, for every command listed by `HELP`, its syntax together with a brief explanation of its purpose. `HELP` SHALL keep displaying the syntax only, and both outputs SHALL list the same commands.

#### Scenario: Extended help content
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every syntax line shown by `HELP` appears, each followed by a non-empty description of what the command does

#### Scenario: Basic help unchanged
- **WHEN** `HELP` is processed
- **THEN** the output lists the command syntaxes without descriptions, including `HELP EXTENDED`

### Requirement: Help formatting behavior is injected
The component that generates help text SHALL receive the behavior that formats its output through dependency injection, so that a new help format can be added by supplying another behavior, without changing how commands are read and executed.

#### Scenario: Alternative format supplied
- **GIVEN** a help generator built with a different formatting behavior
- **WHEN** help is generated
- **THEN** the output follows the supplied behavior and command processing is unchanged
