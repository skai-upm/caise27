# system-date Specification

## Purpose

Provides one application-wide simulated current date that time-dependent operations use instead of the computer clock, so behavior can be tested at any date.

## Requirements

### Requirement: Single current system date
The application SHALL keep exactly one current system date, owned by the main controller and shared by all operations. When the application starts, the system date SHALL be the operating system's current date. The system date SHALL NOT be persisted between executions.

#### Scenario: Initial date
- **WHEN** the application starts on a computer whose date is 2026-10-26 and no `SYSTEM DATE` command has been received
- **THEN** the current system date is 2026-10-26

#### Scenario: Not persisted
- **WHEN** the system date is set to 2030-01-01 and the application is restarted
- **THEN** the current system date is again the operating system's current date

### Requirement: Set system date command
The command `SYSTEM DATE;yyyy-MM-dd` SHALL set the current system date when its single parameter is a valid calendar date in exactly that format. The date MAY move backward or forward without restriction. Any other value SHALL be rejected with an error and the current date SHALL remain unchanged.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is received
- **THEN** the current system date becomes 2026-09-15 and a confirmation is printed

#### Scenario: Moving backward
- **WHEN** the date is 2026-09-15 and `SYSTEM DATE;2025-01-01` is received
- **THEN** the current system date becomes 2025-01-01

#### Scenario: Incorrect format
- **WHEN** any of `SYSTEM DATE;15/09/2026`, `SYSTEM DATE;2026-9-15`, `SYSTEM DATE;2026-02-30` or `SYSTEM DATE;hoy` is received
- **THEN** an invalid-date error is printed and the current system date is unchanged

#### Scenario: Missing parameter
- **WHEN** `SYSTEM DATE` is received without a date
- **THEN** a wrong-number-of-parameters error is printed and the current system date is unchanged

### Requirement: Time-dependent rules use the system date
Every business rule that depends on the current date SHALL obtain it from the current system date and SHALL NOT read the computer's real date directly; a change of the system date SHALL affect all subsequent operations immediately.

#### Scenario: Change applies immediately
- **WHEN** the system date is changed and a time-dependent command is executed next
- **THEN** that command evaluates its rules against the new system date
