# Spec Delta

## Purpose

Defines the single central element that manages the overall park state during an execution, including the current system date and the current base ticket price.

## ADDED Requirements

### Requirement: Single central park manager
The application SHALL have exactly one object that manages the overall park state during an execution and centralises access to users, groups, tickets and attractions. No second instance MUST be obtainable from any part of the application.

#### Scenario: Same instance everywhere
- **WHEN** central park management is requested twice from different parts of the application
- **THEN** the same instance is returned both times

### Requirement: Initial system date
When the application starts, the current system date SHALL be the local date at the time execution begins.

#### Scenario: Date at start-up
- **WHEN** the application starts on 2026-10-29
- **THEN** the current system date is 2026-10-29

### Requirement: SYSTEM DATE command
`SYSTEM DATE;<yyyy-MM-dd>` SHALL replace the current system date; from then on every date-dependent operation uses the new date. The date MAY be changed any number of times in one execution. A value that is not a valid `yyyy-MM-dd` date MUST be rejected without changing the date.

#### Scenario: Change the date
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** the current system date is 2027-04-15 and date-dependent operations use it

#### Scenario: Invalid date rejected
- **WHEN** `SYSTEM DATE;15/04/2027` or `SYSTEM DATE;2027-02-30` is processed
- **THEN** an error is shown and the current system date is unchanged

### Requirement: Current base price
The current base ticket price SHALL be 40.00 euros at the start of each execution. `SYSTEM PRICE;<Price>` SHALL change it for the rest of the current execution only; the value MUST be a valid positive monetary amount (digits with an optional decimal point and at most two decimals) or it is rejected without change.

#### Scenario: Default price
- **WHEN** a ticket is created without SYSTEM PRICE having been used
- **THEN** its price is 40.00 euros

#### Scenario: Price changed
- **WHEN** `SYSTEM PRICE;45.50` is processed and then a ticket is created
- **THEN** the ticket costs 45.50 euros

#### Scenario: Invalid price rejected
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-3`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;10.999` is processed
- **THEN** an error is shown and the price is unchanged

#### Scenario: Existing tickets keep their price
- **WHEN** a ticket is created at 40.00 and then `SYSTEM PRICE;45.50` is processed
- **THEN** the existing ticket still costs 40.00

#### Scenario: Price reset on new execution
- **WHEN** SYSTEM PRICE was changed in a previous execution and the application starts again
- **THEN** the current base price is 40.00 euros
