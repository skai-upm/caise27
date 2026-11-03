# system-date Specification

## Purpose

Provides one simulated current date for the entire application so that time-dependent operations can be executed and tested without using the computer's real date.

## Requirements

### Requirement: Startup date
At the start of every execution the current system date SHALL be the operating system's local date. The configured date SHALL NOT be persisted between executions.

#### Scenario: Fresh execution
- **GIVEN** a new execution in which no `SYSTEM DATE` command has been processed
- **WHEN** the current system date is inspected
- **THEN** it equals the local date of the machine at startup

#### Scenario: Override not persisted
- **GIVEN** an execution that processed `SYSTEM DATE;2026-09-15`
- **WHEN** the application is started again
- **THEN** the current system date is again the local date of the machine

### Requirement: Set the system date
The command `SYSTEM DATE;yyyy-MM-dd` SHALL set the current system date to the given date and report success. Any valid date SHALL be accepted, earlier or later than the current one.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is processed
- **THEN** a success response is returned and the current system date is 2026-09-15

#### Scenario: Moving backward and forward
- **GIVEN** the current system date is 2026-09-15
- **WHEN** `SYSTEM DATE;2020-01-01` and then `SYSTEM DATE;2030-12-31` are processed
- **THEN** after each command the current system date equals the date just set

### Requirement: Reject invalid dates
A `SYSTEM DATE` command whose value is not a real calendar date in the exact format `yyyy-MM-dd`, or that has a missing or extra parameter, SHALL be rejected with an error and SHALL NOT change the current system date.

#### Scenario: Wrong format
- **GIVEN** the current system date is 2026-09-15
- **WHEN** any of `SYSTEM DATE;15/09/2026`, `SYSTEM DATE;2026-9-15`, `SYSTEM DATE;hoy` is processed
- **THEN** an error is reported and the current system date is still 2026-09-15

#### Scenario: Non-existent calendar date
- **WHEN** `SYSTEM DATE;2026-02-30` is processed
- **THEN** an error is reported and the current system date is unchanged

#### Scenario: Missing parameter
- **WHEN** `SYSTEM DATE` is processed without a date
- **THEN** an error is reported and the current system date is unchanged

### Requirement: Single application-wide date
There SHALL be exactly one current system date per execution, held by the application's main controller; every time-dependent business rule SHALL obtain the date from it and SHALL NOT read the computer's real date directly. A change SHALL take effect for every subsequent operation.

#### Scenario: Change visible to subsequent operations
- **GIVEN** `SYSTEM DATE;2026-09-15` was processed
- **WHEN** any later operation asks the controller for the current date
- **THEN** it obtains 2026-09-15

#### Scenario: Works in both input modes
- **GIVEN** a command file containing `SYSTEM DATE;2026-09-15`
- **WHEN** it is processed in file mode
- **THEN** the result is the same as when the command is typed interactively
