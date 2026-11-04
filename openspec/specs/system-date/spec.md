# system-date Specification

## Purpose

Provides one simulated current date for the whole application run, settable by command, so that time-dependent behavior can be executed and tested without depending on the computer's real date.

## Requirements

### Requirement: Single current date initialised at startup
The application SHALL hold exactly one current system date, owned by the main controller and shared by all functionality during a run. At startup it SHALL equal the operating system's local date. The value SHALL NOT be persisted between runs.

#### Scenario: Date before any override
- **GIVEN** a new execution started on the local date D
- **WHEN** the current system date is inspected before any `SYSTEM DATE` command
- **THEN** it is D

#### Scenario: Not persisted
- **GIVEN** a run in which `SYSTEM DATE;2026-09-15` was processed
- **WHEN** the application is started again
- **THEN** the current system date is the operating system's local date of the new start, not 2026-09-15

### Requirement: SYSTEM DATE command
The system SHALL accept `SYSTEM DATE;yyyy-MM-dd` with exactly one parameter: a four-digit year, two-digit month and two-digit day separated by `-`, denoting an existing calendar date. On success it SHALL set the current system date to that value, print a line starting with `OK`, and every subsequent operation SHALL use the new date. The date MAY be moved backward or forward.

#### Scenario: Set a valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is processed
- **THEN** the result starts with `OK` and the current system date is 2026-09-15

#### Scenario: Move backward and forward
- **GIVEN** the current system date is 2026-09-15
- **WHEN** `SYSTEM DATE;2025-01-01` and later `SYSTEM DATE;2027-12-31` are processed
- **THEN** after the first command the current date is 2025-01-01 and after the second it is 2027-12-31

#### Scenario: Leap day boundary
- **WHEN** `SYSTEM DATE;2028-02-29` is processed
- **THEN** it is accepted, while `SYSTEM DATE;2026-02-29` is rejected

### Requirement: Invalid dates leave the date unchanged
A `SYSTEM DATE` command whose parameter is not in the exact `yyyy-MM-dd` format, is not a real calendar date, or whose parameter count is not one SHALL be rejected with a line starting with `ERROR`, and the current system date SHALL keep its previous value.

#### Scenario: Wrong formats
- **GIVEN** the current system date is 2026-09-15
- **WHEN** any of `SYSTEM DATE;15-09-2026`, `SYSTEM DATE;2026/09/15`, `SYSTEM DATE;2026-9-15`, `SYSTEM DATE;2026-13-01`, `SYSTEM DATE;2026-02-30`, `SYSTEM DATE;hoy` or `SYSTEM DATE;` is processed
- **THEN** the result starts with `ERROR` and the current system date is still 2026-09-15

#### Scenario: Wrong number of parameters
- **WHEN** `SYSTEM DATE` or `SYSTEM DATE;2026-09-15;2026-09-16` is processed
- **THEN** the result starts with `ERROR` and the current system date is unchanged

### Requirement: Time-dependent logic uses the system date
Every time-dependent business rule SHALL obtain the current date from the application's system date and SHALL NOT read the computer's real date directly. Only the startup initialisation SHALL read the operating system date.

#### Scenario: Code review of date access
- **WHEN** the production sources are reviewed
- **THEN** the operating system's date is read only when initialising the system date at startup
