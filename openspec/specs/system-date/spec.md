# system-date Specification

## Purpose

Provides one simulated current date for the whole application so that time-dependent rules can be executed and tested without using the computer's real date.

## Requirements

### Requirement: Single system date
The application SHALL hold exactly one current system date, owned by its main controller, and every time-dependent rule SHALL use that date instead of reading the computer's clock.

#### Scenario: Rules use the configured date
- **WHEN** the system date is set to `2026-10-16` and a ticket dated `2026-10-15` exists
- **THEN** every rule evaluates that ticket as past, regardless of the computer's real date

### Requirement: Initial system date
At the start of every execution the system date SHALL be the operating system's current date. The system date SHALL NOT be persisted.

#### Scenario: New execution starts with today
- **WHEN** the application starts, even after a previous run changed the system date
- **THEN** the system date equals the operating system's current date

### Requirement: SYSTEM DATE command
The command `SYSTEM DATE;yyyy-MM-dd` SHALL set the system date to a valid calendar date in ISO `yyyy-MM-dd` format and print `OK SYSTEM DATE -> <date>`. The date MAY move backward or forward freely.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is processed
- **THEN** the system date becomes 2026-09-15 and `OK SYSTEM DATE -> 2026-09-15` is printed

#### Scenario: Moving the date backward
- **WHEN** the system date is 2026-10-16 and `SYSTEM DATE;2026-10-01` is processed
- **THEN** the system date becomes 2026-10-01

#### Scenario: Incorrect format rejected
- **WHEN** `SYSTEM DATE;15/09/2026`, `SYSTEM DATE;2026-9-15` or `SYSTEM DATE;2026-02-30` is processed
- **THEN** an `ERROR` result is printed and the system date is unchanged

### Requirement: Date changes apply immediately
A change of system date SHALL affect every subsequent operation in the same execution.

#### Scenario: Restriction lifted by advancing the date
- **WHEN** a user cannot be deleted because of a ticket dated 2026-10-15 and `SYSTEM DATE;2026-10-16` is processed
- **THEN** the next `USER DELETE` for that user is no longer blocked by that ticket
