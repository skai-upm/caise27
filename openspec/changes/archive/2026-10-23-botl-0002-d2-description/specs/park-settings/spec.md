# Spec Delta

## Purpose

Defines the park's current system date and base ticket price, which date-dependent and price-dependent operations use, and the commands that change them during an execution.

## ADDED Requirements

### Requirement: Initial system state
At the start of every execution the current system date SHALL be the local date when execution begins and the base ticket price SHALL be 40.00 euros, regardless of values set in previous executions.

#### Scenario: Fresh execution
- **WHEN** the application starts after a previous run had set `SYSTEM PRICE;45.50`
- **THEN** new tickets cost 40.00 until the price is changed again

### Requirement: Set system date
The command `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace the current system date with the given valid calendar date; it MAY be used any number of times; an invalid date SHALL be rejected with an error and leave the date unchanged.

#### Scenario: Change date
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** every subsequent date-dependent operation treats 2027-04-15 as today

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;2027-02-30` or `SYSTEM DATE;15/04/2027` is processed
- **THEN** an error is printed and the system date is unchanged

### Requirement: Set base price
The command `SYSTEM PRICE;<Price>` SHALL set the base price for tickets created afterwards; the price SHALL be a positive decimal amount with at most two decimal places; tickets already created SHALL keep their price.

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;45.50` is processed and then a ticket is created
- **THEN** the new ticket costs 45.50 and earlier tickets keep their original price

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-3`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;1.234` is processed
- **THEN** an error is printed and the price is unchanged
