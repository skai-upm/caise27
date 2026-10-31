# system-date Specification

## Purpose

Provides one configurable current date for the whole application so that time-dependent park rules can be exercised and tested independently of the computer's real date.

## Requirements

### Requirement: Single runtime system date
The application SHALL hold exactly one current system date, initialised at startup to the operating system's current local date. The system date SHALL NOT be persisted between executions.

#### Scenario: Startup date
- **WHEN** the application starts
- **THEN** the system date equals the operating system's local date of that day

#### Scenario: Date not persisted
- **WHEN** `SYSTEM DATE;2030-01-01` is executed, the application terminates and starts again
- **THEN** the system date is again the operating system's current local date

### Requirement: SYSTEM DATE command
`SYSTEM DATE;yyyy-MM-dd` SHALL set the system date when the value is a valid calendar date in that format, moving it forward or backward freely. A malformed or non-existent date SHALL be rejected and the previous system date SHALL remain unchanged.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is executed
- **THEN** the command succeeds and the system date becomes 2026-09-15

#### Scenario: Moving backward
- **WHEN** the system date is 2026-10-16 and `SYSTEM DATE;2026-10-01` is executed
- **THEN** the system date becomes 2026-10-01

#### Scenario: Invalid date
- **WHEN** the system date is 2026-09-15 and `SYSTEM DATE;15/09/2026`, `SYSTEM DATE;2026-02-30` or `SYSTEM DATE;` is executed
- **THEN** an error is reported and the system date remains 2026-09-15

### Requirement: Date rules use the system date
Every date-dependent business rule (ticket purchase, ticket refund, user deletion, group locking and attraction entry) SHALL evaluate against the current system date and SHALL NOT consult the operating system date directly. A change of the system date SHALL affect all subsequent operations immediately.

#### Scenario: Date change affects subsequent rules
- **WHEN** a ticket for Ana is dated 2026-10-15, the system date is 2026-10-01 and `USER DELETE` for Ana is rejected, then `SYSTEM DATE;2026-10-16` is executed and `USER DELETE` for Ana is repeated
- **THEN** the second deletion succeeds
