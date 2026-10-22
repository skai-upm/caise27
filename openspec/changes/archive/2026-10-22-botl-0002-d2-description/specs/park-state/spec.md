# Spec Delta

## Purpose

Defines the park-wide state that date- and price-dependent operations rely on: the current system date and the current base ticket price.

## ADDED Requirements

### Requirement: System date
At startup the system date SHALL be the local date of the machine. `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace it for the rest of the execution (any number of times); every date-dependent operation SHALL use the current system date. A date that is not a valid `yyyy-MM-dd` calendar date SHALL be rejected with an error and leave the date unchanged.

#### Scenario: Change the date
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** the system date becomes 2027-04-15 and a confirmation is printed

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;2027-02-30` is processed
- **THEN** an error is printed and the system date is unchanged

### Requirement: Base ticket price
At the start of every execution the base ticket price SHALL be 40.00 euros. `SYSTEM PRICE;<Price>` SHALL set it to a positive amount with at most two decimals for the rest of the execution; the value SHALL NOT be persisted. Changing the price SHALL NOT alter tickets already created.

#### Scenario: Change the price
- **WHEN** `SYSTEM PRICE;45.50` is processed and then an individual ticket is created
- **THEN** the ticket price is 45.50

#### Scenario: Invalid price
- **WHEN** any of `SYSTEM PRICE;0`, `SYSTEM PRICE;-3`, `SYSTEM PRICE;abc`, `SYSTEM PRICE;10.999` is processed
- **THEN** an error is printed and the price is unchanged

#### Scenario: Price resets on restart
- **WHEN** the price is changed to 45.50 and the application is started again
- **THEN** the base price is 40.00
