# park-management Specification

## Purpose

Provides the single central controller of the park state, including the current system date and the current base ticket price used by date- and price-dependent operations.

## Requirements

### Requirement: Single central park controller
The application SHALL have exactly one central park-management object per execution, which centralises access to users, groups, tickets and attractions. The design SHALL guarantee that no second instance can be created from anywhere in the application.

#### Scenario: Same instance everywhere
- **WHEN** the central park controller is requested from two different places in the application
- **THEN** both obtain the same instance

#### Scenario: No public construction
- **WHEN** the park controller class is inspected
- **THEN** it exposes no public constructor

### Requirement: Initial system date and price
At start-up the current system date SHALL be the local date when execution begins and the current base ticket price SHALL be 40.00 euros. Neither value SHALL be persisted between executions.

#### Scenario: Defaults at start
- **WHEN** the application starts on a machine whose local date is D, regardless of any previous SYSTEM DATE or SYSTEM PRICE in earlier executions
- **THEN** the system date is D and the base price is 40.00

### Requirement: SYSTEM DATE command
`SYSTEM DATE;<yyyy-MM-dd>` SHALL set the current system date used by all date-dependent operations. It MAY be used any number of times. An invalid or non-existent date SHALL be rejected without changing the current date.

#### Scenario: Set date
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** subsequent date-dependent operations treat 2027-04-15 as today

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;2027-02-30` or `SYSTEM DATE;15/04/2027` is processed
- **THEN** an error is reported and the system date is unchanged

### Requirement: SYSTEM PRICE command
`SYSTEM PRICE;<Price>` SHALL set the base price for tickets created afterwards. The price SHALL be a positive amount written with digits and an optional `.` followed by at most two decimals. Existing tickets SHALL keep their price.

#### Scenario: Set price
- **WHEN** `SYSTEM PRICE;45.50` is processed and then an individual ticket is created
- **THEN** the new ticket costs 45.50

#### Scenario: Existing tickets unchanged
- **WHEN** a ticket is created at 40.00 and then `SYSTEM PRICE;45.50` is processed
- **THEN** the earlier ticket still costs 40.00

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc`, `SYSTEM PRICE;1e2` or `SYSTEM PRICE;10.123` is processed
- **THEN** an error is reported and the price is unchanged
