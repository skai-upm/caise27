# system-settings Specification

## Purpose

Provides the single simulated current date used by every time-dependent rule and the configurable individual ticket price.

## Requirements

### Requirement: Set the system date
`SYSTEM DATE;yyyy-MM-dd` SHALL set the application-wide current date to a valid calendar date given in exactly that format. The date MAY move backward or forward freely.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is processed
- **THEN** `OK` is reported and the current system date becomes 2026-09-15

#### Scenario: Move date backward
- **WHEN** the system date is 2026-10-16 and `SYSTEM DATE;2026-10-01` is processed
- **THEN** the current system date becomes 2026-10-01

#### Scenario: Wrong format or impossible date
- **WHEN** `SYSTEM DATE;15/09/2026`, `SYSTEM DATE;2026-9-15` or `SYSTEM DATE;2026-02-30` is processed
- **THEN** an `ERROR` is reported and the system date is unchanged

### Requirement: Single date used by all time rules
There SHALL be exactly one current date for the whole application and every time-dependent rule SHALL use it instead of the computer's real date; a change SHALL affect all subsequent operations immediately.

#### Scenario: Date change affects next operation
- **WHEN** a ticket dated 2026-10-15 exists, the system date is changed from 2026-10-01 to 2026-10-16 and the ticket holder is deleted
- **THEN** the deletion is evaluated against 2026-10-16

### Requirement: Initial date is the operating-system date
At every start the system date SHALL be initialized to the operating system's current date; it SHALL NOT be persisted.

#### Scenario: Fresh start
- **WHEN** the application starts after a previous run changed the system date
- **THEN** the system date equals the operating system's current date

### Requirement: Configurable ticket price
`SYSTEM PRICE;<amount>` SHALL set the individual ticket price to a positive amount with at most two decimals. The price SHALL be 40.00 at every start and SHALL NOT be persisted. Prices SHALL be displayed with two decimals.

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;60.00` is processed and then a ticket is bought
- **THEN** the ticket costs 60.00

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;-5`, `SYSTEM PRICE;0` or `SYSTEM PRICE;abc` is processed
- **THEN** an `ERROR` is reported and the price is unchanged
