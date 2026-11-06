# Spec Delta

## Purpose

Defines the single central object that maintains and coordinates the overall Amusement Park state during an execution — its users and other elements, the current system date and the current base ticket price — and the commands that change that state.

## ADDED Requirements

### Requirement: Single central park management
The application SHALL have exactly one central park management object per execution, responsible for the overall park state and for access to the users (and later groups, tickets and attractions); every component that requests central management MUST obtain that same instance, and no other instance can be created.

#### Scenario: Same instance everywhere
- **GIVEN** a running execution
- **WHEN** several components request central park management
- **THEN** they all receive the same instance

#### Scenario: No second instance can be created
- **WHEN** code outside the central management attempts to create a new instance
- **THEN** it cannot, because instances are only obtained through the central access point

### Requirement: Current system date
At start-up the current system date SHALL be the local date at the moment execution begins. `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace it, any number of times during an execution, and from then on every date-dependent operation MUST use the configured date. An invalid or impossible date MUST be rejected with an error message, leaving the current date unchanged.

#### Scenario: Initial date is today
- **WHEN** the application starts
- **THEN** the current system date equals the local date of the computer at start-up

#### Scenario: Date replaced
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** a confirmation is printed and the current system date is 15 April 2027

#### Scenario: Date replaced again
- **GIVEN** `SYSTEM DATE;2027-04-15` was processed
- **WHEN** `SYSTEM DATE;2027-05-01` is processed
- **THEN** the current system date is 1 May 2027

#### Scenario: Invalid date rejected
- **WHEN** `SYSTEM DATE;2027-02-30` or `SYSTEM DATE;15/04/2027` is processed
- **THEN** an error reporting an invalid date is printed and the current date is unchanged

### Requirement: Current base ticket price
At the start of each execution the current base ticket price SHALL be 40.00 euros. `SYSTEM PRICE;<Price>` SHALL replace it with a valid, positive monetary amount (digits with an optional decimal point and at most two decimals); the value applies only to the current execution. Any other value MUST be rejected with an error message, leaving the price unchanged.

#### Scenario: Initial price
- **WHEN** an execution starts
- **THEN** the current base ticket price is 40.00

#### Scenario: Price replaced
- **WHEN** `SYSTEM PRICE;45.50` is processed
- **THEN** a confirmation is printed and the current base price is 45.50

#### Scenario: Invalid price rejected
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;10.999` is processed
- **THEN** an error reporting an invalid price is printed and the price is unchanged

#### Scenario: Price reverts on a new execution
- **GIVEN** an execution in which `SYSTEM PRICE;45.50` was processed
- **WHEN** a new execution starts
- **THEN** the current base price is 40.00 again

### Requirement: System commands take one field
`SYSTEM DATE` and `SYSTEM PRICE` SHALL take exactly one field; any other number of fields MUST be rejected with an error message and change nothing.

#### Scenario: Missing value
- **WHEN** `SYSTEM DATE` is processed without a value
- **THEN** an error reporting the wrong number of fields is printed
