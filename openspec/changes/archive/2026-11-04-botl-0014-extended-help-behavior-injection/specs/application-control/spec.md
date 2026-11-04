## ADDED Requirements

### Requirement: HELP EXTENDED command
The `HELP EXTENDED` command SHALL print a response whose first line starts with `OK:` followed, for every registered command, by its syntax and a brief description of its behaviour; parameters after it SHALL be rejected with an `ERROR:` response.

#### Scenario: Extended help lists syntax and descriptions
- **WHEN** `HELP EXTENDED` is processed
- **THEN** for each command listed by `HELP` the response shows the same syntax followed by a non-empty description, and no data changes

#### Scenario: Extended help with parameters
- **WHEN** `HELP EXTENDED;x` is processed
- **THEN** an `ERROR:` response is printed

### Requirement: Help built from registered commands
`HELP` and `HELP EXTENDED` SHALL both be generated from the single registry of commands supported by the application, so they list the same commands in the same order, including `HELP EXTENDED` itself.

#### Scenario: Same commands in both modes
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** the syntax lines shown by both are identical and in the same order

### Requirement: Injected help output behaviour
The component that builds help text SHALL receive the behaviour used to format its output (and the command registry) through dependency injection at initialization, and SHALL NOT be part of general command parsing and execution, so a new help format can be added by providing another formatting behaviour.

#### Scenario: Alternative behaviour configured
- **GIVEN** a help component initialized with an alternative formatting behaviour
- **WHEN** it generates the help
- **THEN** the output is the one produced by that behaviour, and the command-processing loop is unchanged
