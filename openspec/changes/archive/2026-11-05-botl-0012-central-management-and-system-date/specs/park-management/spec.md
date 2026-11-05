# Spec Delta

## Purpose

Defines the single central manager of the park state during an execution, together with the current system date and the current base ticket price that park operations depend on.

## ADDED Requirements

### Requirement: Single central park manager
The application SHALL have exactly one central park management object per execution, which centralises access to the park elements (users, and later groups, tickets and attractions). Every request for central management, from any part of the application, SHALL obtain that same instance, and the design MUST prevent creating another one.

#### Scenario: Components obtain the same instance
- **GIVEN** a running execution
- **WHEN** central park management is requested from two different components
- **THEN** both receive the same instance

#### Scenario: No second instance can be created
- **WHEN** code outside the central manager tries to construct a new instance
- **THEN** it cannot do so, because no accessible constructor exists

### Requirement: Current system date
The system SHALL maintain a current date, initialised to the local date when execution starts. `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace it with the given date and print `SYSTEM DATE UPDATED`; from then on, all date-dependent operations use the new date. The date MAY be changed any number of times in one execution.

#### Scenario: Initial date is today
- **WHEN** the application starts
- **THEN** the current system date equals the local date at start-up

#### Scenario: Date configured
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** `SYSTEM DATE UPDATED` is printed and the current system date is 2027-04-15

#### Scenario: Date changed repeatedly
- **WHEN** `SYSTEM DATE;2027-04-15` and then `SYSTEM DATE;2026-01-01` are processed
- **THEN** the current system date is 2026-01-01

#### Scenario: Invalid date rejected
- **WHEN** `SYSTEM DATE;2027-02-30`, `SYSTEM DATE;15/04/2027` or `SYSTEM DATE` is processed
- **THEN** an error message starting with `ERROR` is printed and the current date is unchanged

### Requirement: Current base ticket price
The system SHALL maintain a current base ticket price of 40.00 euros at the start of every execution. `SYSTEM PRICE;<Price>` SHALL set it to the given amount and print `SYSTEM PRICE UPDATED` when the amount is a valid positive monetary amount (digits with an optional decimal point and at most two decimals). The price SHALL affect only the current execution and only tickets created afterwards.

#### Scenario: Initial price
- **WHEN** the application starts
- **THEN** the current base price is 40.00

#### Scenario: Price configured
- **WHEN** `SYSTEM PRICE;45.50` is processed
- **THEN** `SYSTEM PRICE UPDATED` is printed and the current base price is 45.50

#### Scenario: Invalid price rejected
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;10.999` is processed
- **THEN** an error message starting with `ERROR` is printed and the current base price is unchanged

#### Scenario: Price reverts in a new execution
- **GIVEN** an execution in which `SYSTEM PRICE;45.50` was processed
- **WHEN** the application is started again
- **THEN** the current base price is 40.00
