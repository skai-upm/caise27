# Spec Delta

## Purpose

Provides the `HELP EXTENDED` command, which explains the purpose of every Amusement Park command next to its syntax, and defines how help output is generated through injected formatting behavior.

## ADDED Requirements

### Requirement: HELP EXTENDED shows syntax and purpose
`HELP EXTENDED` SHALL print every available command with its syntax and a brief explanation of its purpose, in the same order as `HELP`, and MUST NOT change any stored data. It takes no fields.

#### Scenario: Each command explained
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command listed by `HELP` appears with its syntax followed by a non-empty purpose, for example `USER CREATE;<firstName>;...` with an explanation that it registers a new user

#### Scenario: Fields are rejected
- **WHEN** `HELP EXTENDED;x` is processed
- **THEN** an error reporting the wrong number of fields is printed

### Requirement: Injected help-generation behavior
The component that generates help text SHALL receive, through dependency injection, the behavior used to build the output, and the command catalog it describes; different formats (syntax-only for `HELP`, syntax with purpose for `HELP EXTENDED`) MUST be supplied as interchangeable behaviors, so new formats can be added without changing command reading and execution.

#### Scenario: A new format plugs in without touching command processing
- **GIVEN** a help generator constructed with a different formatting behavior
- **WHEN** it generates help for the same catalog
- **THEN** the output follows the injected behavior and the command processing code is unchanged
