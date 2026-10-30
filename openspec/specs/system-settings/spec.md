# system-settings Specification

## Purpose

Provides the single runtime system date and base ticket price used by every date- or price-dependent park rule, so behavior can be tested without depending on the real calendar.

## Requirements

### Requirement: Single runtime system date
The application SHALL hold exactly one current system date, initialized at startup to the operating system's current local date and not persisted. Every business rule that depends on the current date SHALL use this system date instead of querying the operating system date.

#### Scenario: Startup date
- **WHEN** the application starts
- **THEN** the system date equals the operating system's local date for that day

#### Scenario: Date used by subsequent operations
- **GIVEN** a running application
- **WHEN** `SYSTEM DATE;2026-09-15` is executed
- **THEN** subsequent date-sensitive commands evaluate against 2026-09-15

### Requirement: SYSTEM DATE command
`SYSTEM DATE;yyyy-MM-dd` SHALL set the system date to the given calendar date, moving it forward or backward. A missing, malformed or non-existent date SHALL be rejected with an error and leave the previous system date unchanged.

#### Scenario: Move date backward
- **GIVEN** the system date is 2026-10-16
- **WHEN** `SYSTEM DATE;2026-10-01` is executed
- **THEN** the system date becomes 2026-10-01

#### Scenario: Invalid date
- **GIVEN** the system date is 2026-09-15
- **WHEN** `SYSTEM DATE;2026-13-01`, `SYSTEM DATE;15/09/2026` or `SYSTEM DATE;2026-02-30` is executed
- **THEN** an error is reported and the system date remains 2026-09-15

### Requirement: Runtime base ticket price
The application SHALL hold a base individual-ticket price initialized to `40.00` on every run and not persisted. `SYSTEM PRICE;<price>` SHALL set it to a positive decimal amount with at most two decimals; invalid values SHALL be rejected without changing it. Changing the price SHALL NOT alter tickets already purchased.

#### Scenario: Change base price
- **WHEN** `SYSTEM PRICE;60.00` is executed and then a valid ticket is created
- **THEN** the ticket is priced `60.00`

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;abc`, `SYSTEM PRICE;-5` or `SYSTEM PRICE;0` is executed
- **THEN** an error is reported and the base price is unchanged

#### Scenario: Price resets on restart
- **GIVEN** the base price was changed to `60.00` in a previous run
- **WHEN** the application starts again
- **THEN** the base price is `40.00`
