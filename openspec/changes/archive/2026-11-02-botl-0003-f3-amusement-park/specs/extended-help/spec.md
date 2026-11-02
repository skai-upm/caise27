# Spec Delta

## Purpose

Adds an explanatory help variant and makes help generation depend on an injected rendering behavior, independent from command reading and execution.

## ADDED Requirements

### Requirement: HELP EXTENDED
`HELP EXTENDED` SHALL display, for every supported command, its syntax together with a brief explanation of its purpose. `HELP` SHALL keep displaying the syntax only.

#### Scenario: Extended help content
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each command of Deliveries 1 to 3 is shown with its syntax and a description

#### Scenario: Plain help unchanged
- **WHEN** `HELP` is processed
- **THEN** the command syntax is shown without descriptions

### Requirement: Injected help generation behavior
The component that generates help text SHALL receive the behavior used to build the output through dependency injection, so new help formats can be added without changing command reading and execution.

#### Scenario: Alternative format
- **WHEN** the help generator is constructed with a different rendering behavior
- **THEN** it produces output in that format from the same command registry
