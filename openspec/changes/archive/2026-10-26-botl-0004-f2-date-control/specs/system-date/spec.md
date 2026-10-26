# Spec Delta

## Purpose

Provides one simulated current date for the whole application so time-dependent operations can be executed and tested without depending on the computer's real date.

## ADDED Requirements

### Requirement: Set the system date
The system SHALL provide the command `SYSTEM DATE;yyyy-MM-dd` that sets the application's current date to the given calendar date and confirms the new value.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is processed
- **THEN** the response starts with `OK` and the current system date is 2026-09-15

#### Scenario: Date can move backward and forward
- **WHEN** `SYSTEM DATE;2026-09-15` and then `SYSTEM DATE;2025-01-01` are processed
- **THEN** the current system date is 2025-01-01

### Requirement: Reject invalid dates
The system SHALL reject a `SYSTEM DATE` value that does not follow `yyyy-MM-dd` or is not a real calendar date, report an error, and keep the previous date.

#### Scenario: Wrong format
- **WHEN** `SYSTEM DATE;15/09/2026` or `SYSTEM DATE;2026-9-15` is processed
- **THEN** an `ERROR` is reported and the current date is unchanged

#### Scenario: Impossible date
- **WHEN** `SYSTEM DATE;2026-02-30` is processed
- **THEN** an `ERROR` is reported and the current date is unchanged

### Requirement: Single current date
The application SHALL have exactly one current date, held by its main controller, and every time-dependent operation SHALL use it instead of the computer's real date. A change SHALL affect all subsequent operations immediately.

#### Scenario: Subsequent operations see the change
- **WHEN** `SYSTEM DATE;2026-10-01` is processed
- **THEN** every component consulting the current date afterwards obtains 2026-10-01

### Requirement: Initial date is the operating-system date
At start-up the current date SHALL equal the operating system's current date; it SHALL NOT be persisted between executions.

#### Scenario: Fresh start
- **WHEN** the application starts after a previous run set `SYSTEM DATE;2020-01-01`
- **THEN** the current date is the operating system's date of the new run
