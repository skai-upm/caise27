# Spec Delta

## Purpose

Defines the single central park controller that coordinates users, groups, tickets and attractions, and the current system date and base ticket price it maintains.

## ADDED Requirements

### Requirement: Single central park controller
The application SHALL have exactly one object responsible for the overall park state during an execution, centralising access to users, groups, tickets and attractions. Every part of the application requesting central management SHALL obtain that same instance.

#### Scenario: Same instance everywhere
- **WHEN** central park management is requested twice from different parts of the application
- **THEN** both requests return the same instance

### Requirement: Initial system date and price
At start-up the current system date SHALL be the local date when execution begins and the base ticket price SHALL be 40.00 euros. Neither value is carried over from a previous execution.

#### Scenario: Fresh execution defaults
- **WHEN** the application starts, even after a previous run changed date and price
- **THEN** the system date is today's local date and the price is 40.00

### Requirement: SYSTEM DATE command
`SYSTEM DATE;<yyyy-MM-dd>` SHALL replace the current system date for all later date-dependent operations of the execution. It MAY be used any number of times. A value that is not a real date in strict `yyyy-MM-dd` format SHALL be rejected without changing the date.

#### Scenario: Date replaced
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** a confirmation is printed and date-dependent operations use 2027-04-15 as the current date

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;2027-02-30` or `SYSTEM DATE;15/04/2027` is processed
- **THEN** an `ERROR:` message is printed and the date is unchanged

### Requirement: SYSTEM PRICE command
`SYSTEM PRICE;<Price>` SHALL set the base price used for tickets created afterwards. The price SHALL be a positive monetary amount (digits with at most two decimals, greater than zero). Existing tickets keep their prices.

#### Scenario: Price replaced
- **WHEN** `SYSTEM PRICE;45.50` is processed and then an individual ticket is created
- **THEN** the new ticket costs 45.50

#### Scenario: Existing tickets unaffected
- **WHEN** a ticket was created at 40.00 and then `SYSTEM PRICE;50` is processed
- **THEN** the existing ticket still costs 40.00

#### Scenario: Invalid price
- **WHEN** any of `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc`, `SYSTEM PRICE;4.555` is processed
- **THEN** an `ERROR:` message is printed and the price is unchanged
