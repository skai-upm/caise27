# Spec Delta

## ADDED Requirements

### Requirement: Extended help
`HELP EXTENDED` SHALL display, for every command recognised by the application, its syntax together with a brief explanation of its purpose, while `HELP` SHALL continue to display the syntax of every command (including `HELP EXTENDED`).

#### Scenario: Extended help content
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each command's syntax line is followed by a short explanation, e.g. `GROUPTICKET CREATE;<identifier>;<group>;<date>` with an explanation of buying a joint ticket for the group's members

#### Scenario: Basic help unchanged
- **WHEN** `HELP` is processed
- **THEN** the syntax of every command is listed without explanations

### Requirement: Injected help-generation behaviour
The component that generates help text SHALL receive the behaviour used to build the output through dependency injection, so that new help variants can be added without changing the logic that reads and executes commands.

#### Scenario: New help variant
- **WHEN** a help generator is created with a different formatting behaviour
- **THEN** it produces that format from the same command catalogue without changes to command processing
