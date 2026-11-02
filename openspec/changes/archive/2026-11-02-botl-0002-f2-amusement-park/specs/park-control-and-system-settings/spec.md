# Spec Delta

## Purpose

Provides the single central park-management element that coordinates users, groups, tickets and attractions, together with the runtime system date and base ticket price used by date- and price-dependent operations.

## ADDED Requirements

### Requirement: Single central park management
The application SHALL have exactly one park-management instance per execution, which centralizes access to users, groups, tickets and attractions; every part of the application that requests central management SHALL obtain that same instance. (F2-C01)

#### Scenario: Same instance everywhere
- **WHEN** central park management is requested from two different places in the application
- **THEN** both obtain the same instance, and it cannot be instantiated a second time

### Requirement: System date
At start-up the current system date SHALL be the local date when execution begins. `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace it with a valid calendar date, any number of times per execution, and every later date-dependent operation SHALL use the new date. (F2-C01)

#### Scenario: Change the date
- **WHEN** `SYSTEM DATE;2027-04-15` is executed
- **THEN** subsequent date checks (ticket creation, deletion, locking, entry) treat 2027-04-15 as today

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;2027-02-30` or `SYSTEM DATE;15/04/2027` is executed
- **THEN** an error is reported and the system date is unchanged

### Requirement: System base price
At the start of each execution the base ticket price SHALL be 40.00 euros. `SYSTEM PRICE;<Price>` SHALL accept only a valid positive monetary amount (digits with at most two decimals, `.` as separator) and set it as the base price for tickets created afterwards; it SHALL NOT alter tickets already created and SHALL NOT persist to the next execution. (F2-C01)

#### Scenario: Price applies only to new tickets
- **WHEN** ticket T1 is created, then `SYSTEM PRICE;45.50` runs, then ticket T2 is created
- **THEN** T1 keeps price 40.00 and T2 is priced 45.50

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;4.555` is executed
- **THEN** an error is reported and the base price is unchanged

#### Scenario: Price resets on restart
- **WHEN** `SYSTEM PRICE;45.50` runs and the application is started again
- **THEN** the base price is 40.00 euros
