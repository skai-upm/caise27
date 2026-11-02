# Spec Delta

## Purpose

Defines `HELP EXTENDED` and a help generator whose output-building behaviour is supplied by dependency injection (F3-C04).

## ADDED Requirements

### Requirement: HELP EXTENDED
`HELP EXTENDED` SHALL display, for every supported command, its syntax together with a brief explanation of its purpose. `HELP` SHALL keep displaying the syntax of the supported commands.

#### Scenario: Extended help describes every command
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command shown by `HELP` is shown with a non-empty description

### Requirement: Injected help strategy
The component that generates help text SHALL receive, through dependency injection, the strategy used to build the output, so that new help formats can be added without changing command reading and execution.

#### Scenario: Same registry, different strategies
- **WHEN** the help generator is built with the syntax strategy and with the extended strategy over the same command registry
- **THEN** both list the same commands, and only the extended one adds descriptions
