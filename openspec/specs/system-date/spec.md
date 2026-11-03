# system-date Specification

## Purpose
Provides one simulated current date for the whole execution, settable by command, so that every time-dependent operation can be run and tested without using the computer's real date.

## Requirements

### Requirement: Single current date initialised at startup
The application SHALL hold exactly one current system date for the whole execution, initialised to the operating system's local date when the application starts, and SHALL NOT persist it between executions.

#### Scenario: Startup date
- **GIVEN** a new execution
- **WHEN** the current date is inspected before any `SYSTEM DATE` command
- **THEN** it equals the operating system's local date at startup

#### Scenario: Not persisted
- **GIVEN** an execution where `SYSTEM DATE;2026-09-15` was processed
- **WHEN** the application is started again
- **THEN** the current date is again the operating system's local date

### Requirement: SYSTEM DATE command
The system SHALL process `SYSTEM DATE;yyyy-MM-dd`, setting the current date to the given date and answering with an `OK:` response; any later or earlier valid date SHALL be accepted.

#### Scenario: Set a valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is processed
- **THEN** an `OK:` response is printed and the current date becomes 2026-09-15

#### Scenario: Move backward and forward
- **GIVEN** the current date is 2026-09-15
- **WHEN** `SYSTEM DATE;2025-01-01` and then `SYSTEM DATE;2030-12-31` are processed
- **THEN** the current date is 2025-01-01 after the first command and 2030-12-31 after the second

### Requirement: Invalid dates rejected
A `SYSTEM DATE` command whose value is not a real calendar date in exactly the `yyyy-MM-dd` format, or that has a missing or extra parameter, SHALL be rejected with an `ERROR:` response and SHALL leave the current date unchanged.

#### Scenario: Wrong format
- **GIVEN** the current date is 2026-09-15
- **WHEN** any of `SYSTEM DATE;15-09-2026`, `SYSTEM DATE;2026/09/15`, `SYSTEM DATE;2026-9-15` or `SYSTEM DATE;hoy` is processed
- **THEN** an `ERROR:` response is printed and the current date remains 2026-09-15

#### Scenario: Impossible calendar date
- **WHEN** `SYSTEM DATE;2026-02-30` or `SYSTEM DATE;2026-13-01` is processed
- **THEN** an `ERROR:` response is printed and the current date is unchanged

#### Scenario: Missing parameter
- **WHEN** `SYSTEM DATE` is processed
- **THEN** an `ERROR:` response is printed and the current date is unchanged

### Requirement: Date-dependent behaviour uses the system date
Every time-dependent operation SHALL use the current system date, and a change of that date SHALL affect all operations processed after it; no business rule SHALL read the computer's real date directly.

#### Scenario: Change takes effect immediately
- **WHEN** `SYSTEM DATE;2026-09-15` is processed
- **THEN** the very next command observes 2026-09-15 as the current date

### Requirement: SYSTEM DATE listed in HELP
The `HELP` response SHALL include the line `SYSTEM DATE;yyyy-MM-dd`.

#### Scenario: Help shows the command
- **WHEN** `HELP` is processed
- **THEN** the response contains `SYSTEM DATE;yyyy-MM-dd`
