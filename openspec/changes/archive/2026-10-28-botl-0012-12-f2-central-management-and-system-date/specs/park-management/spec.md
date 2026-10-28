# Spec Delta

## Purpose

Defines the central management of the park: a single controller owning the overall park state during an execution, the current system date used by date-dependent operations, and the current base ticket price.

## ADDED Requirements

### Requirement: Single park controller
The application SHALL have exactly one park controller per execution, centralizing access to the park state (users now; groups, tickets and attractions as they are added). Every part of the application that requests central management SHALL obtain that same instance, and no second instance SHALL be creatable.

#### Scenario: Same instance everywhere
- **WHEN** central park management is requested twice from different parts of the application
- **THEN** the same controller instance is returned both times

#### Scenario: No public construction
- **WHEN** the controller class is inspected
- **THEN** it offers no accessible constructor that would allow creating another instance

### Requirement: Initial system date
At the start of each execution the current system date SHALL be the local date of the machine at the moment execution begins.

#### Scenario: Startup date
- **WHEN** the application starts on 2026-10-28
- **THEN** the current system date is 2026-10-28

### Requirement: SYSTEM DATE command
The command `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace the current system date with the given valid calendar date, print `SYSTEM DATE UPDATED`, and from then on all date-dependent operations SHALL use it; it MAY be issued any number of times. An invalid or wrongly formatted date SHALL be rejected with an `ERROR:` message, leaving the date unchanged.

#### Scenario: Change the date
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** `SYSTEM DATE UPDATED` is printed and the current system date is 2027-04-15

#### Scenario: Change the date again
- **WHEN** `SYSTEM DATE;2027-04-15` and then `SYSTEM DATE;2027-05-01` are processed
- **THEN** the current system date is 2027-05-01

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;15/04/2027` or `SYSTEM DATE;2027-02-30` is processed
- **THEN** an `ERROR:` line about the date is printed and the current system date is unchanged

### Requirement: Base ticket price
At the start of each execution the current base ticket price SHALL be 40.00 euros, regardless of any price set in a previous execution.

#### Scenario: Startup price
- **WHEN** the application starts, even after a previous execution changed the price
- **THEN** the current base ticket price is 40.00

### Requirement: SYSTEM PRICE command
The command `SYSTEM PRICE;<Price>` SHALL set the current base ticket price to the given amount and print `SYSTEM PRICE UPDATED`. The price SHALL be a valid positive monetary amount (a decimal number greater than zero with at most two decimals); otherwise it SHALL be rejected with an `ERROR:` message, leaving the price unchanged.

#### Scenario: Change the price
- **WHEN** `SYSTEM PRICE;45.50` is processed
- **THEN** `SYSTEM PRICE UPDATED` is printed and the current base ticket price is 45.50

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;12.345` is processed
- **THEN** an `ERROR:` line about the price is printed and the current base ticket price is unchanged
