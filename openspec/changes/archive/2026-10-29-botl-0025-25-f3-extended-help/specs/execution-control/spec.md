# Spec Delta

## ADDED Requirements

### Requirement: HELP EXTENDED command
The command `HELP EXTENDED` SHALL print, for every command recognized by the application, its syntax together with a brief explanation of its purpose, without changing any data. `HELP` SHALL keep printing the syntax list, which also includes `HELP EXTENDED`.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command syntax shown by `HELP` appears with an explanation next to it

#### Scenario: Basic help unchanged
- **WHEN** `HELP` is processed
- **THEN** it shows the command syntax list without explanations

### Requirement: Injected help generation
The component that generates help text SHALL receive, through dependency injection, the behavior used to build the output; supporting a new kind of help SHALL only require providing a new behavior and wiring it, without changing the logic that reads and executes commands.

#### Scenario: Alternative help behavior
- **WHEN** the help generator is constructed with a different output behavior
- **THEN** it produces that output for the same command catalog, and the command processor is unchanged
