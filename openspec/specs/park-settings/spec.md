# park-settings Specification

## Purpose

Holds the park-wide settings that date- and price-dependent operations rely on: the current system date and the current base ticket price.

## Requirements

### Requirement: Initial system date
At startup the current system date SHALL be the local date of the machine running the application. All date-dependent operations SHALL use the current system date, never the machine clock directly.

#### Scenario: Startup date
- **WHEN** the application starts and no `SYSTEM DATE` command has been processed
- **THEN** the current system date equals the local date at startup

### Requirement: SYSTEM DATE command
`SYSTEM DATE;<yyyy-MM-dd>` SHALL set the current system date to the given calendar date and print a confirmation. It MAY be used any number of times in a run, moving the date forward or backward. An invalid or non-existent date SHALL be rejected with an error beginning with `ERROR`, leaving the date unchanged.

#### Scenario: Date changed
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** subsequent operations treat 15 April 2027 as the current date

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;2027-02-30` or `SYSTEM DATE;15/04/2027` is processed
- **THEN** an error is printed and the current date is unchanged

### Requirement: Initial ticket price
At the start of every run the current base ticket price SHALL be 40.00 EUR, regardless of any price set in a previous run.

#### Scenario: Price resets on restart
- **WHEN** `SYSTEM PRICE;45.50` is processed, the application exits, and a new run starts
- **THEN** a newly created individual ticket costs 40.00 EUR

### Requirement: SYSTEM PRICE command
`SYSTEM PRICE;<price>` SHALL set the current base ticket price. The price SHALL be a positive amount written as digits with an optional `.` and at most two decimals. Invalid values SHALL be rejected with an error beginning with `ERROR`, leaving the price unchanged. Changing the price SHALL NOT alter the price of existing tickets.

#### Scenario: Price changed
- **WHEN** `SYSTEM PRICE;45.50` is processed and then an individual ticket is created
- **THEN** the new ticket costs 45.50 EUR

#### Scenario: Existing tickets keep their price
- **WHEN** a ticket is created at 40.00 EUR and then `SYSTEM PRICE;45.50` is processed
- **THEN** the existing ticket still costs 40.00 EUR

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-3`, `SYSTEM PRICE;abc`, or `SYSTEM PRICE;10.555` is processed
- **THEN** an error is printed and the price is unchanged
