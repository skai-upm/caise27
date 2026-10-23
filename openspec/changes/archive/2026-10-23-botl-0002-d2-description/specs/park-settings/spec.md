# Spec Delta

## Purpose

Holds the park-wide settings that date-dependent and price-dependent operations rely on: the current system date and the current base ticket price.

## ADDED Requirements

### Requirement: Current system date
The application SHALL keep a current system date, initialised to the local date when execution starts. Every date-dependent operation (ticket creation, deletion, active/historical checks, attraction entry) SHALL use this date rather than the computer clock. It is not persisted between runs.

#### Scenario: Initial date
- **WHEN** the application starts and no `SYSTEM DATE` command has been processed
- **THEN** the system date is the computer's local date at start-up

### Requirement: SYSTEM DATE command
`SYSTEM DATE;<yyyy-MM-dd>` SHALL replace the current system date with the given valid calendar date, any number of times per execution, and confirm the new date. An invalid or wrongly formatted date SHALL be rejected with an error and leave the date unchanged.

#### Scenario: Change date
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** the system date becomes 2027-04-15 and later operations treat it as today

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;2027-02-30` or `SYSTEM DATE;15/04/2027` is processed
- **THEN** an error is printed and the system date is unchanged

### Requirement: Current base ticket price
The application SHALL keep a current base ticket price of 40.00 EUR at the start of every execution. It is used as the base price for tickets created afterwards and is not persisted between runs.

#### Scenario: Default price
- **WHEN** a ticket is created and `SYSTEM PRICE` has not been used in this execution
- **THEN** the ticket's base price is 40.00 EUR

### Requirement: SYSTEM PRICE command
`SYSTEM PRICE;<Price>` SHALL set the current base price to a positive monetary amount written as digits with an optional decimal point and at most two decimals. Invalid, zero or negative amounts SHALL be rejected with an error. Changing the price SHALL NOT alter tickets already created.

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;45.50` is processed and a ticket is then created
- **THEN** the new ticket costs 45.50 EUR while tickets created earlier keep their original price

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;-3`, `SYSTEM PRICE;0`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;1.999` is processed
- **THEN** an error is printed and the price is unchanged

#### Scenario: Price resets on restart
- **WHEN** the price was changed to 45.50 and the application is started again
- **THEN** the base price is 40.00 EUR
