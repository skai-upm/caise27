# Spec Delta

## Purpose

Defines the single central park controller and the global park state it owns: the current system date and the current base ticket price.

## ADDED Requirements

### Requirement: Single central park controller
The application SHALL have exactly one central park management object per execution, which gives access to the users, groups, tickets and attractions. The design SHALL guarantee that no second instance can be created, wherever in the application access is requested.

#### Scenario: Same instance everywhere
- **WHEN** central park management is requested from two different places in the application
- **THEN** both obtain the same instance

### Requirement: Initial system date
When the application starts, the current system date SHALL be the local date at the start of execution.

#### Scenario: Start-up date
- **WHEN** the application starts and no `SYSTEM DATE` command has been run
- **THEN** date-dependent operations use today's local date

### Requirement: SYSTEM DATE command
`SYSTEM DATE;<yyyy-MM-dd>` SHALL replace the current system date for the rest of the execution, and it MAY be used any number of times. An invalid or non-existent date SHALL be rejected with an error and leave the date unchanged.

#### Scenario: Change date
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** all later date-dependent operations treat 2027-04-15 as the current date

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;2027-02-30` or `SYSTEM DATE;15/04/2027` is processed
- **THEN** an error is printed and the current date is unchanged

### Requirement: Current base ticket price
The current base ticket price SHALL be 40.00 euros at the start of every execution. `SYSTEM PRICE;<Price>` SHALL replace it with a valid positive monetary amount (digits with an optional decimal point and at most two decimals) for the rest of the execution only. Invalid amounts SHALL be rejected and leave the price unchanged.

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;45.50` is processed and then a ticket is created
- **THEN** the new ticket's base price is 45.50

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-3`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;1.234` is processed
- **THEN** an error is printed and the price is unchanged

#### Scenario: Price resets on restart
- **WHEN** the price is changed, the application exits, and it is started again
- **THEN** the current base price is 40.00

#### Scenario: Existing tickets unaffected
- **WHEN** a ticket is created at 40.00 and the price is then changed to 45.50
- **THEN** the existing ticket's price remains 40.00
