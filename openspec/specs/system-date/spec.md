# system-date Specification

## Purpose

Defines the single simulated current date used by every time-dependent operation, so behavior can be tested without depending on the computer's real date.

## Requirements

### Requirement: Initial current date
At startup the application's current date SHALL be the operating system's current date. The current date SHALL NOT be persisted between executions.

#### Scenario: Fresh start
- **WHEN** the application starts, even after a previous run set another date
- **THEN** the current date equals the operating system's date on that day

### Requirement: Set the current date
`SYSTEM DATE;yyyy-MM-dd` SHALL set the application's current date to the given date and respond `OK SYSTEM DATE -> yyyy-MM-dd`. The date MAY be moved backwards or forwards any number of times.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is processed
- **THEN** the response is `OK SYSTEM DATE -> 2026-09-15` and the current date is 2026-09-15

#### Scenario: Moving backwards
- **WHEN** `SYSTEM DATE;2026-09-15` and then `SYSTEM DATE;2025-01-01` are processed
- **THEN** both are accepted and the current date is 2025-01-01

### Requirement: Date format validation
The date SHALL be a real calendar date written exactly as `yyyy-MM-dd`. Any other value SHALL be rejected with `ERROR INVALID DATE`, leaving the current date unchanged.

#### Scenario: Wrong format
- **WHEN** `SYSTEM DATE;15/09/2026`, `SYSTEM DATE;2026-9-15` or `SYSTEM DATE;hoy` is processed
- **THEN** the response is `ERROR INVALID DATE` and the current date does not change

#### Scenario: Impossible date
- **WHEN** `SYSTEM DATE;2026-02-30` is processed
- **THEN** the response is `ERROR INVALID DATE`

### Requirement: Single date for time-dependent rules
There SHALL be exactly one current date in the application, owned by the main controller and shared with every component. Every time-dependent business rule SHALL use this date and SHALL NOT consult the computer's real date; a change of the date SHALL affect all subsequent operations immediately.

#### Scenario: Shared date
- **WHEN** `SYSTEM DATE;2026-10-01` is processed
- **THEN** every component that reads the current date afterwards obtains 2026-10-01
