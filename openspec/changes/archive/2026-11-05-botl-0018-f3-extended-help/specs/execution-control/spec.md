# Spec Delta

## ADDED Requirements

### Requirement: HELP EXTENDED command
The command `HELP EXTENDED` SHALL display every command that `HELP` lists, each with its syntax and a brief explanation of its purpose. `HELP` SHALL keep displaying the syntax list only.

#### Scenario: Extended help shows purposes
- **GIVEN** interactive or file input
- **WHEN** `HELP EXTENDED` is processed
- **THEN** for every command listed by `HELP` (for example `GROUPTICKET CREATE;<identifier>;<group>;<date>`), the output shows its syntax followed by a non-empty purpose description

#### Scenario: Plain help unchanged
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax lines only, with no purpose descriptions

### Requirement: Help generation behavior is injected
The component that generates help text SHALL receive, through dependency injection, the behavior used to build its output. It SHALL be independent of the logic that reads and executes commands, so that new help variants can be added by supplying a new behavior without changing command processing.

#### Scenario: New help format plugged in
- **GIVEN** a help generator constructed with a custom formatting behavior
- **WHEN** help is generated
- **THEN** the output is produced by that behavior from the same command descriptions, and command processing code is unchanged
