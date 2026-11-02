# Spec Delta

## Purpose

Offers a second help format that explains each command's purpose, with help generation driven by an injected formatting strategy decoupled from command execution.

## ADDED Requirements

### Requirement: HELP EXTENDED
`HELP EXTENDED` SHALL display, for every supported command, its syntax together with a brief explanation of its purpose, while `HELP` keeps displaying the syntax only. (F3-C04)

#### Scenario: Extended output
- **WHEN** `HELP EXTENDED` is executed
- **THEN** every command listed by HELP appears with its syntax and a non-empty description

### Requirement: Injected help strategy
The component that generates help text SHALL receive the behavior used to build the output through dependency injection, so new help formats can be added without changing command reading and execution. (F3-C04)

#### Scenario: Same registry, different strategies
- **WHEN** HELP and HELP EXTENDED are generated
- **THEN** both are produced from the same command registry by the same generator type configured with different injected strategies
