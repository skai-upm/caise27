# Spec Delta

## Purpose

Provides one simulated current date for the whole application so that time-dependent park rules can be executed and tested independently of the computer's real date.

## ADDED Requirements

### Requirement: Runtime system date initialization
At startup the application SHALL initialize a single system date to the operating system's current local date. The system date SHALL NOT be persisted between executions.

#### Scenario: Startup date
- **WHEN** the application starts
- **THEN** the system date equals the operating system's current local date

### Requirement: Set system date command
`SYSTEM DATE;yyyy-MM-dd` SHALL set the system date to a valid calendar date in exactly that format, moving it forward or backward. A malformed or invalid date SHALL be rejected and the previous system date SHALL remain unchanged.

#### Scenario: Date updated for subsequent operations
- **GIVEN** a running application
- **WHEN** `SYSTEM DATE;2026-09-15` is executed
- **THEN** subsequent date-sensitive commands evaluate against 2026-09-15

#### Scenario: Moving backward
- **GIVEN** the system date is 2026-09-15
- **WHEN** `SYSTEM DATE;2026-01-01` is executed
- **THEN** the system date becomes 2026-01-01

#### Scenario: Invalid date
- **GIVEN** the system date is 2026-09-15
- **WHEN** `SYSTEM DATE;15/09/2026` or `SYSTEM DATE;2026-02-30` is executed
- **THEN** the command is rejected and the system date remains 2026-09-15

### Requirement: Centralized date for business rules
All date-dependent business rules (ticket creation, ticket and group-ticket deletion, user deletion, group locking and attraction entry) SHALL evaluate against the system date and SHALL NOT query the operating system date.

#### Scenario: Rules follow the configured date
- **GIVEN** a ticket dated 2026-10-15
- **WHEN** the system date is set to 2026-10-15 and then to 2026-10-16
- **THEN** the ticket counts as current on the first date and as historical on the second, regardless of the real date
