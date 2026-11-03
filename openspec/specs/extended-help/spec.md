# extended-help Specification

## Purpose

Explains each command in more detail on demand and keeps help generation extensible to new formats without coupling it to command processing.

## Requirements

### Requirement: Extended help command
The command `HELP EXTENDED` SHALL display, for every supported command, its syntax and a brief description of its behaviour. It SHALL not change any data.

#### Scenario: Extended content
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each supported command appears with its syntax followed by a non-empty description, e.g. `TICKET DELETE;Id` with an explanation that only future tickets can be deleted

#### Scenario: Parameters rejected
- **WHEN** `HELP EXTENDED;x` is processed
- **THEN** an error is reported

### Requirement: Help from the registered commands
`HELP` and `HELP EXTENDED` SHALL be generated from the same registry of supported commands, and every command in that registry SHALL be recognised by the command processor.

#### Scenario: Same commands in both modes
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** both list exactly the same command syntaxes in the same order

#### Scenario: Registry matches the processor
- **WHEN** the operation of each registered command is processed
- **THEN** none is reported as an unknown command

### Requirement: Injected help behaviour
The component that builds help text SHALL receive the behaviour that formats the output through dependency injection and SHALL NOT instantiate it itself; a different formatting behaviour SHALL be usable without changing the command-processing loop.

#### Scenario: Alternative formatter
- **GIVEN** a help generator configured with an alternative formatting behaviour
- **WHEN** the command processor is created with it and `HELP` is processed
- **THEN** the output is produced by the alternative behaviour
