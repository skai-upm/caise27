# Spec Delta

## Purpose

Defines the current individual-ticket price used when tickets are created and the command to change it during an execution.

## ADDED Requirements

### Requirement: Initial ticket price
Each execution SHALL start with a current ticket price of 40.00. The price SHALL NOT be persisted between executions.

#### Scenario: Default price
- **GIVEN** a new execution
- **WHEN** a ticket is created before any `SYSTEM PRICE` command
- **THEN** its price is 40.00

#### Scenario: Price resets on restart
- **GIVEN** a previous execution processed `SYSTEM PRICE;60.00`
- **WHEN** the application starts again and a ticket is created
- **THEN** its price is 40.00

### Requirement: Change ticket price
`SYSTEM PRICE;<price>` SHALL set the current ticket price when the value is a positive decimal number made of digits with an optional `.` and at most two decimals. Tickets created afterwards SHALL capture the new price; existing tickets SHALL keep the price they were created with.

#### Scenario: New price applies to later tickets only
- **GIVEN** ticket `T001` created at 40.00
- **WHEN** `SYSTEM PRICE;60.00` is processed and ticket `T002` is created
- **THEN** the reply to the price command is an `OK` message, `T002` reports `OK TICKET CREATED -> 60.00`, and `T001` still has price 40.00

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;abc`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;0`, `SYSTEM PRICE;10.999`, `SYSTEM PRICE;` or `SYSTEM PRICE` is processed
- **THEN** an `ERROR` message is produced and the current price is unchanged
