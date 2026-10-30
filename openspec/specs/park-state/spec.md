# park-state Specification

## Purpose

Defines the single central park manager that coordinates users, groups, tickets and attractions, together with the current system date and base ticket price.

## Requirements

### Requirement: Single central park manager
The application SHALL have exactly one central park management object per execution, centralising access to users, groups, tickets and attractions; every part of the application that requests it SHALL obtain the same instance.

#### Scenario: Same instance everywhere
- **WHEN** central park management is requested twice from different parts of the application
- **THEN** both requests return the same instance

### Requirement: Current system date
At start-up the system date SHALL be the local date. `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace it for all date-dependent operations, as many times as needed; a value that is not a valid `yyyy-MM-dd` date SHALL be rejected with an error and leave the date unchanged.

#### Scenario: Initial date
- **WHEN** the application starts
- **THEN** the system date equals the local date of the machine

#### Scenario: Set date
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** the system date is 2027-04-15 and date-dependent operations use it

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;2027-13-40` or `SYSTEM DATE;15/04/2027` is processed
- **THEN** an error is printed and the system date is unchanged

### Requirement: Current base ticket price
Each execution SHALL start with a base price of 40.00 euros. `SYSTEM PRICE;<Price>` SHALL set it to a valid positive monetary amount (at most two decimals), used only for tickets created afterwards and only for the current execution. Invalid, zero or negative values SHALL be rejected.

#### Scenario: Default price
- **WHEN** a ticket is created without any prior `SYSTEM PRICE`
- **THEN** its price is 40.00 euros

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;45.50` is processed and then a ticket is created
- **THEN** the new ticket costs 45.50 euros and tickets created before keep their price

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;-3`, `SYSTEM PRICE;0`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;1.234` is processed
- **THEN** an error is printed and the price is unchanged

#### Scenario: Price resets on restart
- **WHEN** the price was changed and the application is started again
- **THEN** the base price is 40.00 euros
