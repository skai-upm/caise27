# Spec Delta

## Purpose

Defines the single central element that manages the overall park state during an execution, including the current system date and the current base ticket price, and the SYSTEM commands that modify them.

## ADDED Requirements

### Requirement: Single central park management instance
The application SHALL have exactly one central park management object per execution, responsible for the overall park state; every part of the application requesting it SHALL obtain the same instance, and no other instance SHALL be creatable.

#### Scenario: Same instance from different components
- **GIVEN** a running execution
- **WHEN** two different components request central park management
- **THEN** both receive the same instance

#### Scenario: No additional instances can be created
- **GIVEN** the central park management type
- **WHEN** code outside it attempts to construct a new instance
- **THEN** construction is not possible

### Requirement: Current system date
The system date SHALL initially be the local date at the moment execution starts. The command `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace it, any number of times per execution, and all date-dependent operations SHALL use the current system date from then on. A missing or invalid date SHALL be rejected with an error message, leaving the date unchanged.

#### Scenario: Initial date is today
- **GIVEN** a new execution
- **WHEN** no `SYSTEM DATE` command has been processed
- **THEN** the current system date equals the local date at start

#### Scenario: Valid date replaces the current date
- **GIVEN** a running execution
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** a confirmation is shown and the current system date is 2027-04-15

#### Scenario: Date may change several times
- **GIVEN** `SYSTEM DATE;2027-04-15` has been processed
- **WHEN** `SYSTEM DATE;2027-05-01` is processed
- **THEN** the current system date is 2027-05-01

#### Scenario: Invalid date rejected
- **GIVEN** the current system date is 2027-04-15
- **WHEN** `SYSTEM DATE;2027-02-30`, `SYSTEM DATE;15/04/2027` or `SYSTEM DATE` is processed
- **THEN** an error message is shown and the current system date is still 2027-04-15

### Requirement: Current base ticket price
The base ticket price SHALL be 40.00 euros at the start of every execution. The command `SYSTEM PRICE;<Price>` SHALL replace it with a valid positive monetary amount (a decimal number with at most two decimal places, greater than zero) for the rest of the execution only. Invalid prices SHALL be rejected with an error message, leaving the price unchanged.

#### Scenario: Initial price
- **GIVEN** a new execution
- **WHEN** no `SYSTEM PRICE` command has been processed
- **THEN** the current base price is 40.00

#### Scenario: Valid price replaces the base price
- **GIVEN** a running execution
- **WHEN** `SYSTEM PRICE;45.50` is processed
- **THEN** a confirmation is shown and the current base price is 45.50

#### Scenario: Invalid price rejected
- **GIVEN** the current base price is 45.50
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-3`, `SYSTEM PRICE;abc`, `SYSTEM PRICE;10.999` or `SYSTEM PRICE` is processed
- **THEN** an error message is shown and the current base price is still 45.50

#### Scenario: Price does not survive a new execution
- **GIVEN** an execution where `SYSTEM PRICE;45.50` was processed
- **WHEN** a new execution starts
- **THEN** the current base price is 40.00
