# Spec Delta

## Purpose

Defines the single central park management state of the Amusement Park application and its system settings, the current date and the current base ticket price, which can be changed during an execution with the SYSTEM commands.

## ADDED Requirements

### Requirement: Single central park management instance
The system SHALL have exactly one central park management object per execution, responsible for the overall park state, and every request for central management from any part of the application SHALL obtain that same instance.

#### Scenario: Several components request central management
- **GIVEN** a running execution
- **WHEN** central park management is requested from two different parts of the application
- **THEN** both receive the same instance
- **AND** a change made through one, such as a new system date, is visible through the other

#### Scenario: No second instance can be created
- **GIVEN** the application code
- **WHEN** code outside the central management component tries to create a new central management object directly
- **THEN** this is not possible

### Requirement: Initial system date
The system SHALL set the current system date to the local date at the moment execution starts.

#### Scenario: Date at startup
- **GIVEN** an execution started on a given local date
- **WHEN** no SYSTEM DATE command has been processed
- **THEN** the current system date equals that local date

### Requirement: SYSTEM DATE command
The system SHALL accept `SYSTEM DATE;<yyyy-MM-dd>` with a valid calendar date and from then on use that date as the current system date for all date-dependent operations. It SHALL allow this any number of times in one execution and SHALL reject a missing or invalid date with an error message, leaving the current date unchanged.

#### Scenario: Set the system date
- **GIVEN** a running execution
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** a confirmation is displayed
- **AND** the current system date is April 15, 2027

#### Scenario: Change the date several times
- **GIVEN** the system date was set to 2027-04-15
- **WHEN** `SYSTEM DATE;2027-05-01` is processed
- **THEN** the current system date is May 1, 2027

#### Scenario: Invalid date is rejected
- **GIVEN** the current system date is 2027-04-15
- **WHEN** `SYSTEM DATE;2027-02-30`, `SYSTEM DATE;15/04/2027` or `SYSTEM DATE` without a value is processed
- **THEN** an error message is displayed
- **AND** the current system date is still 2027-04-15

### Requirement: Initial base ticket price
The system SHALL set the current base ticket price to 40.00 euros at the start of every execution, regardless of any price set in a previous execution.

#### Scenario: Price at startup
- **GIVEN** a new execution
- **WHEN** no SYSTEM PRICE command has been processed
- **THEN** the current base ticket price is 40.00 euros

### Requirement: SYSTEM PRICE command
The system SHALL accept `SYSTEM PRICE;<Price>` where the price is a valid, positive monetary amount with at most two decimal places, and use it as the current base ticket price for the rest of the execution. It SHALL reject zero, negative, non-numeric or missing values with an error message, leaving the current price unchanged.

#### Scenario: Set the base price
- **GIVEN** a running execution
- **WHEN** `SYSTEM PRICE;45.50` is processed
- **THEN** a confirmation is displayed
- **AND** the current base ticket price is 45.50 euros

#### Scenario: Invalid price is rejected
- **GIVEN** the current base ticket price is 40.00 euros
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc`, `SYSTEM PRICE;10.999` or `SYSTEM PRICE` without a value is processed
- **THEN** an error message is displayed
- **AND** the current base ticket price is still 40.00 euros

### Requirement: SYSTEM commands listed in HELP
The system SHALL include `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>` in the output of `HELP`.

#### Scenario: HELP shows SYSTEM commands
- **WHEN** `HELP` is processed
- **THEN** the output contains `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`
