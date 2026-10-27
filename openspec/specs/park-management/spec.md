# park-management Specification

## Purpose

Defines the single central park controller that coordinates the park's overall state, including the current system date and the current base ticket price.

## Requirements

### Requirement: Single central park controller
The application SHALL have exactly one central park management object per execution, which centralises access to the park's users (and later groups, tickets and attractions); every request for it, from any part of the application, SHALL obtain the same instance.

#### Scenario: Same instance everywhere
- **WHEN** the central park controller is requested twice from different parts of the application
- **THEN** both requests return the same instance

#### Scenario: No public construction
- **WHEN** the controller type is inspected
- **THEN** it offers no public constructor

### Requirement: Current system date
At start-up the system date SHALL be the local date when execution begins; `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace it, as many times as needed, for all subsequent date-dependent operations.

#### Scenario: Initial date
- **WHEN** the application starts
- **THEN** the system date equals the local date of the machine at that moment

#### Scenario: Change the date
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** the response is `SYSTEM DATE UPDATED` and the current system date is 2027-04-15

#### Scenario: Invalid date rejected
- **WHEN** `SYSTEM DATE;2027-02-30`, `SYSTEM DATE;15/04/2027` or `SYSTEM DATE` is processed
- **THEN** an error is reported and the system date is unchanged

### Requirement: Current base ticket price
At the start of each execution the base ticket price SHALL be 40.00 euros; `SYSTEM PRICE;<Price>` SHALL replace it with a valid, positive monetary amount for the rest of the current execution only.

#### Scenario: Initial price
- **WHEN** the application starts, even after a previous execution changed the price
- **THEN** the base ticket price is 40.00

#### Scenario: Change the price
- **WHEN** `SYSTEM PRICE;45.50` is processed
- **THEN** the response is `SYSTEM PRICE UPDATED` and the base price is 45.50

#### Scenario: Invalid price rejected
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc`, `SYSTEM PRICE;45.505` or `SYSTEM PRICE;1e3` is processed
- **THEN** an error is reported and the base price is unchanged
