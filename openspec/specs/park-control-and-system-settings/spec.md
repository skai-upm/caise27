# park-control-and-system-settings Specification

## Purpose

Provides the single central park manager and the runtime system settings (current date and base ticket price) on which all date- and price-dependent park operations rely (source F2-C01, sections 3, 4.1 and 4.2).

## Requirements

### Requirement: Single central park manager
The application SHALL have exactly one object responsible for the overall park state during an execution, centralizing access to users, groups, tickets and attractions; the design SHALL guarantee that no second instance can be created from anywhere in the application.

#### Scenario: Same instance everywhere
- **WHEN** central park management is requested from two different places in the application
- **THEN** both obtain the same instance, and no public way exists to construct another

### Requirement: Current system date
At start-up the current system date SHALL be the local date when execution begins; `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace it for all later date-dependent operations of the same execution, any number of times.

#### Scenario: Date replaced
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** success is reported and subsequent date checks treat 2027-04-15 as the current date

#### Scenario: Initial date is today
- **WHEN** the application starts and no `SYSTEM DATE` has been processed
- **THEN** the current system date equals the local date at start-up

#### Scenario: Invalid date rejected
- **WHEN** `SYSTEM DATE;2027-13-45` or `SYSTEM DATE;15/04/2027` is processed
- **THEN** an error is reported and the current system date is unchanged

### Requirement: Current base ticket price
The base ticket price SHALL be 40.00 euros at the start of every execution; `SYSTEM PRICE;<Price>` SHALL set it to a valid positive monetary amount for tickets created afterwards in the same execution, without changing already created tickets.

#### Scenario: Price affects only new tickets
- **WHEN** a ticket is created at 40.00, then `SYSTEM PRICE;45.50` is processed and another ticket is created
- **THEN** the first ticket keeps price 40.00 and the second has price 45.50

#### Scenario: Invalid price rejected
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;10.999` is processed
- **THEN** an error is reported and the base price is unchanged

#### Scenario: Price resets on a new execution
- **WHEN** `SYSTEM PRICE;45.50` is processed, the application exits and is started again
- **THEN** the base price is 40.00
