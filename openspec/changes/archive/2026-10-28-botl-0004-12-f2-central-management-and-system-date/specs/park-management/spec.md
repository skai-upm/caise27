# Spec Delta

## Purpose

Centralizes the overall park state in a single park controller and maintains the current system date and current base ticket price used by date- and price-dependent operations.

## ADDED Requirements

### Requirement: Single park controller
The application SHALL have exactly one park controller instance per execution, responsible for the overall park state; every part of the application requesting central management SHALL obtain that same instance.

#### Scenario: Same instance from any access point
- **WHEN** central park management is requested twice from different parts of the application
- **THEN** both requests obtain the same instance

### Requirement: Initial system state
At the start of an execution the current system date SHALL be the local date at which execution starts and the current base ticket price SHALL be 40.00 euros.

#### Scenario: State at start-up
- **WHEN** a new execution starts
- **THEN** the current system date equals the local date of the computer and the current base price is 40.00

### Requirement: SYSTEM DATE command
The system SHALL accept `SYSTEM DATE;<yyyy-MM-dd>` and, from that moment on, use the given date as the current system date. The date MAY be changed any number of times during one execution. A value that is missing or not a valid date in `yyyy-MM-dd` format SHALL be rejected with an error message, leaving the current date unchanged.

#### Scenario: Set the system date
- **WHEN** the command `SYSTEM DATE;2027-04-15` is processed
- **THEN** the current system date is April 15, 2027

#### Scenario: Change the date several times
- **WHEN** `SYSTEM DATE;2027-04-15` and then `SYSTEM DATE;2027-05-01` are processed
- **THEN** the current system date is May 1, 2027

#### Scenario: Invalid date rejected
- **WHEN** `SYSTEM DATE;2027-02-30`, `SYSTEM DATE;15/04/2027` or `SYSTEM DATE` is processed
- **THEN** an error message is displayed and the current system date is unchanged

### Requirement: SYSTEM PRICE command
The system SHALL accept `SYSTEM PRICE;<Price>` and use the given valid, positive monetary amount as the current base ticket price for the rest of the execution. A missing, non-numeric, zero, negative or otherwise invalid amount SHALL be rejected with an error message, leaving the price unchanged.

#### Scenario: Set the base price
- **WHEN** the command `SYSTEM PRICE;45.50` is processed
- **THEN** the current base price is 45.50

#### Scenario: Invalid price rejected
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc`, `SYSTEM PRICE;45.505` or `SYSTEM PRICE` is processed
- **THEN** an error message is displayed and the current base price is unchanged

### Requirement: Price is per execution
A price set with `SYSTEM PRICE` SHALL affect only the current execution; a new execution SHALL start again with 40.00 euros.

#### Scenario: Price reverts on new execution
- **WHEN** `SYSTEM PRICE;45.50` is processed and the application is started again
- **THEN** the current base price of the new execution is 40.00
