# Spec Delta

## Purpose

Provides one simulated current date for the whole execution so time-dependent rules can be exercised and tested without consulting the computer's real date.

## ADDED Requirements

### Requirement: Single current system date
The application SHALL hold exactly one current system date per execution, owned by its main controller, and every time-dependent operation SHALL obtain the date from it rather than from the computer's clock.

#### Scenario: All operations see the same date
- **GIVEN** the current date was set to `2026-09-15`
- **WHEN** any operation asks the controller for the current date
- **THEN** it obtains `2026-09-15`

### Requirement: Startup date
At the start of every execution the current system date SHALL be the operating system's local date; the configured date SHALL NOT be persisted between executions.

#### Scenario: New execution
- **GIVEN** a new execution with no `SYSTEM DATE` command processed
- **WHEN** the current date is inspected
- **THEN** it equals the operating system's local date at startup

#### Scenario: Override not persisted
- **GIVEN** an execution that processed `SYSTEM DATE;2026-09-15` and ended
- **WHEN** a new execution starts
- **THEN** its current date is again the operating system's local date

### Requirement: SYSTEM DATE command
The command `SYSTEM DATE;yyyy-MM-dd` SHALL set the current system date to the given calendar date, accepting dates earlier or later than the current one, and the new date SHALL apply to every operation processed afterwards.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is processed
- **THEN** a success message is printed and subsequent operations use `2026-09-15`

#### Scenario: Moving backward and forward
- **GIVEN** the current date is `2026-09-15`
- **WHEN** `SYSTEM DATE;2020-01-01` and then `SYSTEM DATE;2030-12-31` are processed
- **THEN** after the first command the current date is `2020-01-01` and after the second it is `2030-12-31`

#### Scenario: Leap day boundary
- **WHEN** `SYSTEM DATE;2028-02-29` is processed
- **THEN** it is accepted

### Requirement: Invalid dates rejected
A `SYSTEM DATE` command whose value is not a real calendar date in the exact `yyyy-MM-dd` format, or that has a missing or extra parameter, SHALL be rejected with an error message and SHALL leave the current date unchanged.

#### Scenario: Wrong format or impossible date
- **GIVEN** the current date is `2026-09-15`
- **WHEN** `SYSTEM DATE;15-09-2026`, `SYSTEM DATE;2026/09/15`, `SYSTEM DATE;2026-9-15`, `SYSTEM DATE;2026-02-30`, `SYSTEM DATE;2027-02-29` or `SYSTEM DATE;hoy` is processed
- **THEN** each is rejected and the current date remains `2026-09-15`

#### Scenario: Missing or extra parameter
- **WHEN** `SYSTEM DATE`, `SYSTEM DATE;` or `SYSTEM DATE;2026-09-15;x` is processed
- **THEN** it is rejected with a usage message and the current date is unchanged
