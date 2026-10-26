# system-date Specification

## Purpose

Provides one simulated current date for the whole application so time-dependent operations can be executed and tested without using the computer's real date.

## Requirements

### Requirement: Set system date
The command `SYSTEM DATE;yyyy-MM-dd` SHALL set the application's current date to the given calendar date and print `OK SYSTEM DATE <date>`. The date MAY be moved backward or forward.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is processed
- **THEN** the current date becomes 2026-09-15 and `OK SYSTEM DATE 2026-09-15` is printed

#### Scenario: Move date backward
- **WHEN** the current date is 2026-09-15 and `SYSTEM DATE;2026-01-01` is processed
- **THEN** the current date becomes 2026-01-01

### Requirement: Reject invalid dates
A `SYSTEM DATE` command whose value is not a real calendar date in `yyyy-MM-dd` format SHALL be rejected with an `ERROR` line and SHALL leave the current date unchanged.

#### Scenario: Wrong format
- **WHEN** `SYSTEM DATE;15/09/2026` is processed
- **THEN** an `ERROR` line is printed and the current date does not change

#### Scenario: Impossible date
- **WHEN** `SYSTEM DATE;2026-02-30` is processed
- **THEN** an `ERROR` line is printed and the current date does not change

### Requirement: Single application date
The application SHALL keep exactly one current date shared by all components, and every time-dependent business rule SHALL use it instead of the computer's real date. A change of the date SHALL affect all subsequent operations immediately.

#### Scenario: Change takes effect immediately
- **WHEN** `SYSTEM DATE;2026-10-16` is processed
- **THEN** the next command that depends on the date evaluates its rules against 2026-10-16

### Requirement: Initial date
At startup the current date SHALL be the operating system's current date; the system date SHALL NOT be persisted between executions.

#### Scenario: Fresh start
- **WHEN** the application starts after the date was set in a previous execution
- **THEN** the current date equals the operating system's date at startup
