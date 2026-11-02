# park-control-and-system-settings Specification

## Purpose

Defines the single central park controller that coordinates users, groups, tickets and attractions, and the runtime system date and base ticket price (F2-C01).

## Requirements

### Requirement: Single central park controller
The application SHALL have exactly one object responsible for managing the overall park state during an execution, centralising access to users, groups, tickets and attractions. The design SHALL guarantee that no second instance can be created, wherever in the application it is requested.

#### Scenario: Same instance everywhere
- **WHEN** central park management is requested from two different places in the application
- **THEN** both obtain the same instance, and no public constructor allows creating another one

### Requirement: System date
At start-up the current system date SHALL be the local date at which execution begins. `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace it with the given date for all later date-dependent operations; it MAY be changed any number of times. A missing or invalid date SHALL be rejected without changing the current date.

#### Scenario: Date changed
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** later date-dependent checks use 2027-04-15 as the current date

#### Scenario: Initial date
- **WHEN** the application starts and no `SYSTEM DATE` has been processed
- **THEN** the current date equals the local date at start-up

#### Scenario: Invalid date rejected
- **WHEN** `SYSTEM DATE;2027-02-30` or `SYSTEM DATE;15/04/2027` is processed
- **THEN** an error is reported and the current date is unchanged

### Requirement: System base price
At the start of each execution the base ticket price SHALL be 40.00 euros. `SYSTEM PRICE;<Price>` SHALL set it to a valid, positive monetary amount (digits with at most two decimals, `.` as decimal separator). The new price SHALL apply only to tickets created afterwards; it SHALL NOT alter existing tickets and SHALL NOT survive to a new execution.

#### Scenario: New price applies to later tickets only
- **WHEN** a ticket is created at 40.00, then `SYSTEM PRICE;45.50` is processed and another ticket is created
- **THEN** the first ticket keeps price 40.00 and the second has price 45.50

#### Scenario: Invalid price rejected
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;10.999` is processed
- **THEN** an error is reported and the base price is unchanged

#### Scenario: Price resets on restart
- **WHEN** the price is changed and the application is started again
- **THEN** the base price is 40.00 euros
