# system-configuration Specification

## Purpose

Provides the single simulated current date used by every time-dependent rule and the configurable price of individual tickets.

## Requirements

### Requirement: Single system date
The application SHALL keep exactly one current date, held by its main controller, and every time-dependent rule SHALL use it instead of the computer's real date. At startup the date SHALL be the operating system's current date; it SHALL NOT be persisted.

#### Scenario: Initial date
- **WHEN** the application starts and no `SYSTEM DATE` command has been processed
- **THEN** the current date equals the operating system's date

### Requirement: SYSTEM DATE command
The system SHALL accept `SYSTEM DATE;yyyy-MM-dd`, set the current date to that value and report `OK SYSTEM DATE -> yyyy-MM-dd`. The date MAY move backward or forward. Values not in the exact `yyyy-MM-dd` format or not a real calendar date SHALL be rejected and the current date SHALL stay unchanged.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is processed
- **THEN** the system reports `OK SYSTEM DATE -> 2026-09-15` and the current date is 2026-09-15

#### Scenario: Invalid format
- **WHEN** `SYSTEM DATE;15/09/2026`, `SYSTEM DATE;2026-9-15` or `SYSTEM DATE;2026-02-30` is processed
- **THEN** the system reports an error and the current date is unchanged

#### Scenario: Change affects later operations
- **WHEN** a ticket dated 2026-10-15 exists, the date is set to 2026-10-01 and then to 2026-10-16
- **THEN** the ticket counts as future after the first command and as past after the second

### Requirement: Individual ticket price
The system SHALL keep the current individual ticket price, initialised to 40.00 on every execution and not persisted. `SYSTEM PRICE;<importe>` SHALL set it to a positive decimal amount with at most two decimals and report `OK SYSTEM PRICE -> <importe>` formatted with two decimals; invalid amounts SHALL be rejected.

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;60.00` is processed and then a ticket is bought
- **THEN** the system reports `OK SYSTEM PRICE -> 60.00` and the ticket costs 60.00

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;-5`, `SYSTEM PRICE;0` or `SYSTEM PRICE;abc` is processed
- **THEN** the system reports an error and the price is unchanged
