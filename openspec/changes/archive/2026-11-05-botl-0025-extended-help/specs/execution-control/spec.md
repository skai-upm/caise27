## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display every available command with its syntax and a brief explanation of its purpose, and SHALL NOT change any data. `HELP` SHALL continue to display the syntax of the recognized commands.

#### Scenario: Extended help lists purpose with syntax
- **WHEN** `HELP EXTENDED` is processed
- **THEN** for every command shown by `HELP` the same syntax appears together with a non-empty purpose text, for example `USER CREATE;<FirstName>;...` followed by an explanation that it registers a new user

#### Scenario: HELP unchanged
- **WHEN** `HELP` is processed
- **THEN** the syntax lines are displayed without purpose explanations

#### Scenario: Extra elements rejected
- **WHEN** `HELP EXTENDED;x` is processed
- **THEN** an error message starting with `ERROR` is printed

### Requirement: Injected help generation behavior
The class responsible for generating help text SHALL receive, through dependency injection during initialization, the behavior used to build the output, so that new help variants can be added by supplying a new behavior without modifying command reading and execution.

#### Scenario: New help variant without changing command processing
- **GIVEN** a help generator constructed with a different output behavior
- **WHEN** it generates the help for the registered commands
- **THEN** the output follows that behavior and the command processor code is unchanged
