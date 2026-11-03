# Spec Delta

## Purpose

Adds an explanatory help variant and makes help generation pluggable through injected formatting behaviour.

## ADDED Requirements

### Requirement: HELP EXTENDED
`HELP EXTENDED` SHALL display, for every supported command, its syntax together with a brief explanation of its purpose, while `HELP` keeps displaying the syntax only.

#### Scenario: Extended help content
- **WHEN** `HELP EXTENDED` is entered
- **THEN** every command of Deliveries 1 to 3 is shown with its syntax and purpose

#### Scenario: Plain help unchanged
- **WHEN** `HELP` is entered
- **THEN** the command syntaxes are shown without purposes

### Requirement: Injected help generation behaviour
The component generating help text SHALL receive, through dependency injection, the behaviour used to build the output, so that new help formats can be added without changing command reading and execution.

#### Scenario: New help format
- **WHEN** the help generator is constructed with a different formatting behaviour
- **THEN** it produces the new format from the same command catalogue
