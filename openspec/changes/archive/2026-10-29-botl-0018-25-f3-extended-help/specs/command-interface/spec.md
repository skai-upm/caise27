# Spec Delta

## ADDED Requirements

### Requirement: HELP EXTENDED command
The command `HELP EXTENDED` SHALL display, for every command recognized by the application, its syntax together with a brief explanation of its purpose, and SHALL NOT stop processing. `HELP` SHALL remain available, keep displaying the command syntax, and include `HELP EXTENDED` in its list.

#### Scenario: Extended help shows syntax and purpose
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every syntax shown by `HELP` appears, each followed by a non-empty description of its purpose

#### Scenario: HELP lists HELP EXTENDED
- **WHEN** `HELP` is processed
- **THEN** the output contains `HELP EXTENDED`

#### Scenario: Extra arguments rejected
- **WHEN** `HELP EXTENDED;x` is processed
- **THEN** an error message is shown and processing continues

### Requirement: Injected help-generation behavior
The component that generates help text SHALL receive, through dependency injection, the behavior used to build the output, so that new ways of generating help can be added without changing the logic that reads and executes commands.

#### Scenario: Alternative formatter injected
- **WHEN** the help generator is created with a different formatting behavior
- **THEN** HELP output follows that behavior while command reading and execution are unchanged
