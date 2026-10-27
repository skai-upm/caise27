# park-management Specification

## Purpose

Defines the central element that maintains the overall park state during one execution: a single park controller holding the current system date, the current base ticket price, and access to the park's users, groups, tickets and attractions.

## Requirements

### Requirement: Single central park controller
The application SHALL have exactly one object responsible for managing the overall park state during an execution, centralising access to the park's elements. No more than one instance SHALL exist during the same execution, regardless of where in the application it is accessed.

#### Scenario: Same instance everywhere
- **WHEN** the central park controller is requested from two different parts of the application
- **THEN** both obtain the same instance

#### Scenario: No public construction
- **WHEN** the controller class is inspected
- **THEN** it offers no public constructor through which a second instance could be created

### Requirement: Current system date
When the application starts, the system date SHALL be the local date at which execution begins. `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace the system date with the given calendar date, and every later date-dependent operation SHALL use it. The date MAY be replaced any number of times in an execution.

#### Scenario: Initial date
- **WHEN** the application starts
- **THEN** the system date equals the local date of the computer at start

#### Scenario: Date replaced
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** a confirmation is printed and the system date is 15 April 2027

#### Scenario: Date replaced again
- **WHEN** `SYSTEM DATE;2027-04-15` and then `SYSTEM DATE;2027-05-01` are processed
- **THEN** the system date is 1 May 2027

#### Scenario: Invalid date rejected
- **WHEN** `SYSTEM DATE;2027-02-30`, `SYSTEM DATE;15-04-2027` or `SYSTEM DATE` is processed
- **THEN** an error is printed and the system date is unchanged

### Requirement: Current base ticket price
At the start of each execution the base ticket price SHALL be 40.00 euros. `SYSTEM PRICE;<Price>` SHALL replace it with the given amount, which MUST be a valid positive monetary amount. The new price SHALL apply only to the current execution.

#### Scenario: Initial price
- **WHEN** the application starts
- **THEN** the current base price is 40.00

#### Scenario: Price replaced
- **WHEN** `SYSTEM PRICE;45.50` is processed
- **THEN** a confirmation is printed and the current base price is 45.50

#### Scenario: Invalid price rejected
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;10.123` is processed
- **THEN** an error is printed and the price is unchanged

#### Scenario: Price resets on new execution
- **WHEN** `SYSTEM PRICE;45.50` is processed, the application ends and a new execution starts
- **THEN** the current base price is 40.00
