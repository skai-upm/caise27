# park-settings Specification

## Purpose

Defines the configurable park state that date- and price-dependent operations rely on: the current system date and the current base ticket price.

## Requirements

### Requirement: Current system date
The system SHALL keep a current system date, initialised to the local date when execution starts. `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace it for all subsequent date-dependent operations and MAY be used any number of times. An invalid date SHALL be rejected with an error and leave the date unchanged. The system date SHALL NOT be persisted.

#### Scenario: Set the date
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** subsequent operations treat 2027-04-15 as today

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;2027-13-45` is processed
- **THEN** an error is printed and the current date is unchanged

### Requirement: Current base ticket price
The system SHALL keep a current base ticket price of 40.00 euros at the start of every execution. `SYSTEM PRICE;<Price>` SHALL set it to a positive monetary amount with at most two decimals; other values SHALL be rejected. The new price SHALL apply only to tickets created afterwards and SHALL NOT be persisted.

#### Scenario: Default price
- **WHEN** a ticket is created and `SYSTEM PRICE` has not been used in this execution
- **THEN** its price is 40.00 euros

#### Scenario: Change the price
- **WHEN** `SYSTEM PRICE;45.50` is processed and a ticket is created afterwards
- **THEN** the new ticket costs 45.50 euros and earlier tickets keep their price

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;-3` or `SYSTEM PRICE;abc` or `SYSTEM PRICE;1.234` is processed
- **THEN** an error is printed and the price is unchanged
