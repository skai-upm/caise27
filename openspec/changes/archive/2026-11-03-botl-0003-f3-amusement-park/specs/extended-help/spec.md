# Spec Delta

## Purpose

Adds an explanatory `HELP EXTENDED` alongside the syntax-only `HELP`, with the help text produced by a formatting behavior injected into the help generator (source F3-C04, section 7).

## ADDED Requirements

### Requirement: HELP EXTENDED
`HELP EXTENDED` SHALL display, for every command recognized by the application, its syntax together with a brief explanation of its purpose.

#### Scenario: Every command explained
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each command syntax shown by `HELP` appears with a non-empty description of its purpose

### Requirement: Injected help formatting
The component that generates help text SHALL receive the behavior used to build the output through dependency injection, so new help formats can be added without changing command reading and execution.

#### Scenario: Formats are interchangeable strategies
- **WHEN** the help generator is constructed with the syntax formatter or the extended formatter
- **THEN** it produces the `HELP` or `HELP EXTENDED` output respectively from the same command registry
