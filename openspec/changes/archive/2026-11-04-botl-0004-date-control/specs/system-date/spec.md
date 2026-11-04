# Spec Delta

## Purpose

Provides a single simulated current date for the whole application run, so time-dependent operations can be executed and tested without depending on the computer's real date.

## ADDED Requirements

### Requirement: Initial system date
At the start of each execution the current system date SHALL equal the operating system's local date at startup. The configured date SHALL NOT be persisted between executions.

#### Scenario: Fresh execution
- **GIVEN** a new execution started on local date D
- **WHEN** the current system date is inspected before any `SYSTEM DATE` command
- **THEN** it equals D

#### Scenario: Not persisted
- **GIVEN** a previous execution set `SYSTEM DATE;2030-01-01`
- **WHEN** the application starts again on local date D
- **THEN** the current system date is D

### Requirement: Set system date
The command `SYSTEM DATE;yyyy-MM-dd` SHALL set the current system date to the given calendar date and report it with an `OK` message. The date MAY be moved backward or forward any number of times.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is processed
- **THEN** an `OK` message is produced and the current system date is 2026-09-15

#### Scenario: Backward and forward
- **GIVEN** the current date was set to 2026-09-15
- **WHEN** `SYSTEM DATE;2020-01-01` and then `SYSTEM DATE;2031-12-31` are processed
- **THEN** after each command the current date equals the date just set

### Requirement: Invalid date rejected
A `SYSTEM DATE` command whose value is not a valid calendar date in exactly the `yyyy-MM-dd` format, or that has a missing or extra parameter, SHALL be rejected with an `ERROR` message and SHALL NOT change the current date.

#### Scenario: Malformed or impossible dates
- **GIVEN** the current date is 2026-09-15
- **WHEN** `SYSTEM DATE;15-09-2026`, `SYSTEM DATE;2026-9-15`, `SYSTEM DATE;2026-02-30`, `SYSTEM DATE;2026-13-01`, `SYSTEM DATE;hoy` or `SYSTEM DATE;` is processed
- **THEN** an `ERROR` message is produced and the current date remains 2026-09-15

#### Scenario: Wrong parameter count
- **WHEN** `SYSTEM DATE` or `SYSTEM DATE;2026-09-15;x` is processed
- **THEN** an `ERROR` message is produced and the current date is unchanged

### Requirement: Single date source
The application SHALL hold exactly one current date, owned by its main command controller, and every time-dependent business rule SHALL obtain the date from it rather than from the computer's clock.

#### Scenario: Change affects subsequent operations immediately
- **GIVEN** the current date is changed with `SYSTEM DATE`
- **WHEN** the next command is processed by the same controller
- **THEN** that command observes the newly configured date
