# system-date Specification

## Purpose

Provides one simulated current date for the whole application so that every time-dependent park rule can be executed and tested without depending on the computer's real date.

## Requirements

### Requirement: Single runtime system date
The application SHALL hold exactly one current system date, initialised at startup to the operating system's current local date. The system date SHALL NOT be persisted between executions.

#### Scenario: Startup date
- **WHEN** the application starts
- **THEN** the system date equals the operating system's current local date, regardless of any date set in a previous execution

### Requirement: SYSTEM DATE command
The `SYSTEM DATE;yyyy-MM-dd` command SHALL set the system date when the parameter is a valid calendar date in exactly that format. The date SHALL be freely movable forward or backward. An invalid or malformed date SHALL be rejected with an error message and the previous system date SHALL remain unchanged.

#### Scenario: Date updated for subsequent operations
- **GIVEN** a running application with a configured date
- **WHEN** `SYSTEM DATE;2026-09-15` is executed
- **THEN** a success message is reported and subsequent date-sensitive commands evaluate against 2026-09-15

#### Scenario: Moving the date backward
- **GIVEN** the system date is 2026-10-16
- **WHEN** `SYSTEM DATE;2026-10-01` is executed
- **THEN** the system date becomes 2026-10-01

#### Scenario: Invalid date
- **GIVEN** the system date is 2026-09-15
- **WHEN** any of `SYSTEM DATE;15-09-2026`, `SYSTEM DATE;2026-02-30`, `SYSTEM DATE;2026-9-5`, or `SYSTEM DATE;tomorrow` is executed
- **THEN** an error message is reported and the system date remains 2026-09-15

### Requirement: Date-dependent rules use the system date
Every business rule that depends on the current date (ticket creation and deletion, user deletion, group locking, attraction entry) SHALL evaluate against the configured system date and SHALL NOT query the operating system's date.

#### Scenario: Changing the date immediately affects rules
- **GIVEN** a ticket dated 2026-10-15 and the system date 2026-10-01
- **WHEN** `SYSTEM DATE;2026-10-16` is executed and the ticket's holder is then deleted
- **THEN** the deletion is evaluated against 2026-10-16
