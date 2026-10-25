# system-configuration Specification

## Purpose

Provides the single simulated current date and the individual ticket price used by all time-dependent and pricing rules of the park.

## Requirements

### Requirement: Single simulated current date
The application SHALL hold exactly one current date, initialised to the operating system date when the application starts and not persisted. Every time-dependent rule SHALL use this date and SHALL NOT consult the real system date.

#### Scenario: Startup date
- **WHEN** the application starts
- **THEN** the current date equals the operating system date of that day

#### Scenario: Rules follow the simulated date
- **WHEN** `SYSTEM DATE;2026-10-16` is executed after a ticket for `2026-10-15` was bought
- **THEN** that ticket is treated as past by all subsequent operations

### Requirement: SYSTEM DATE command
`SYSTEM DATE;yyyy-MM-dd` SHALL set the current date to any valid calendar date, earlier or later than the current one, taking effect immediately. Dates not in the exact `yyyy-MM-dd` format or not existing in the calendar SHALL be rejected leaving the date unchanged.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is executed
- **THEN** the reply starts with `OK` and the current date is 2026-09-15

#### Scenario: Going backwards
- **WHEN** the date is 2026-09-15 and `SYSTEM DATE;2026-01-01` is executed
- **THEN** the current date becomes 2026-01-01

#### Scenario: Invalid format
- **WHEN** `SYSTEM DATE;15/09/2026` or `SYSTEM DATE;2026-02-30` is executed
- **THEN** an error is reported and the current date does not change

### Requirement: SYSTEM PRICE command
The individual ticket price SHALL be initialised to 40.00 at each start (not persisted). `SYSTEM PRICE;<amount>` SHALL set it to a positive decimal amount used by subsequent ticket purchases; invalid or non-positive amounts SHALL be rejected.

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;60.00` is executed and then a ticket is created
- **THEN** the reply is `OK TICKET CREATED -> 60.00`

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;abc` or `SYSTEM PRICE;-5` is executed
- **THEN** an error is reported and the price is unchanged
