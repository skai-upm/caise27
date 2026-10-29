# park-management Specification

## Purpose

Defines the single central park-management state of an execution, including the current system date and the current base ticket price, and the SYSTEM commands that change them.

## Requirements

### Requirement: Single central park management
The application SHALL have exactly one central park-management object per execution, and every part of the application that requests central management SHALL obtain that same instance.

#### Scenario: Same instance from different access points
- **WHEN** central park management is requested twice from different parts of the application
- **THEN** both requests return the same instance

#### Scenario: No public construction
- **WHEN** code outside the central management element attempts to create a new instance directly
- **THEN** this is not possible because no public constructor is available

### Requirement: Initial system date and price
When execution starts, the current system date SHALL be the local date at that moment and the current base ticket price SHALL be 40.00 euros.

#### Scenario: Defaults at start of execution
- **WHEN** the application starts and no SYSTEM command has been processed
- **THEN** the current system date equals the local date and the current base price is 40.00

### Requirement: SYSTEM DATE command
The command `SYSTEM DATE;<yyyy-MM-dd>` SHALL set the current system date to the given date, which all date-dependent operations SHALL then regard as the current date. It MAY be used any number of times during an execution. A value that is not a valid calendar date in the format `yyyy-MM-dd`, or a wrong number of arguments, SHALL be rejected with an error message and the date SHALL remain unchanged.

#### Scenario: Set the system date
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** a confirmation is shown and the current system date is 2027-04-15

#### Scenario: Change the date several times
- **WHEN** `SYSTEM DATE;2027-04-15` and then `SYSTEM DATE;2028-01-01` are processed
- **THEN** the current system date is 2028-01-01

#### Scenario: Invalid date rejected
- **WHEN** `SYSTEM DATE;2027-02-30` or `SYSTEM DATE;15-04-2027` or `SYSTEM DATE` is processed
- **THEN** an error message is shown and the current system date is unchanged

### Requirement: SYSTEM PRICE command
The command `SYSTEM PRICE;<Price>` SHALL set the current base ticket price for the current execution only. The price SHALL be a valid, positive monetary amount (plain decimal number with `.` separator and at most two decimal places, greater than zero); otherwise, or with a wrong number of arguments, an error message SHALL be shown and the price SHALL remain unchanged.

#### Scenario: Set the price
- **WHEN** `SYSTEM PRICE;45.50` is processed
- **THEN** a confirmation is shown and the current base price is 45.50

#### Scenario: Non-positive or malformed price rejected
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-3`, `SYSTEM PRICE;abc`, `SYSTEM PRICE;45,50` or `SYSTEM PRICE;1.234` is processed
- **THEN** an error message is shown and the current base price is unchanged

#### Scenario: Price reverts in a new execution
- **WHEN** a new execution of the application starts after a previous execution changed the price
- **THEN** the current base price is 40.00
