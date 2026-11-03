# Spec Delta

## Purpose

Provides the single central park manager and the runtime settings it coordinates: the current system date and the current base ticket price.

## ADDED Requirements

### Requirement: Single central park manager
A single object SHALL manage the overall park state, centralising access to users, groups, tickets and attractions; no more than one instance SHALL exist during an execution, wherever access is requested.

#### Scenario: Same instance everywhere
- **WHEN** the central manager is requested from different parts of the application
- **THEN** the same instance is returned every time

### Requirement: System date
The system date SHALL initially be the local date when execution starts and SHALL be replaceable any number of times with `SYSTEM DATE;<yyyy-MM-dd>`; all date-dependent operations SHALL use the current system date.

#### Scenario: Changing the date
- **WHEN** `SYSTEM DATE;2027-04-15` is executed
- **THEN** subsequent date-dependent checks treat 2027-04-15 as today

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;15/04/2027` is executed
- **THEN** an error is reported and the system date is unchanged

### Requirement: System base price
The base ticket price SHALL be 40.00 euros at the start of every execution and SHALL be changeable with `SYSTEM PRICE;<Price>` to a valid, positive monetary amount; it SHALL apply only to tickets created afterwards and SHALL NOT be persisted.

#### Scenario: Price affects only new tickets
- **WHEN** a ticket is created, then `SYSTEM PRICE;45.50` runs and a second ticket is created
- **THEN** the first ticket keeps 40.00 and the second costs 45.50

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;1.234` is executed
- **THEN** an error is reported and the price is unchanged

#### Scenario: Price resets on a new execution
- **WHEN** the price was changed and the application is started again
- **THEN** the base price is 40.00
