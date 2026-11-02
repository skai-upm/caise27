# Spec Delta

## Purpose

Centralizes the park state in a single management object per execution, including the current system date and the current base ticket price, adjustable at runtime through SYSTEM commands.

## ADDED Requirements

### Requirement: Single central park management
The application SHALL have exactly one object responsible for the overall park state during an execution, centralizing access to users, groups, tickets and attractions. The design SHALL guarantee that no second instance can be created, wherever access is requested.

#### Scenario: Same instance everywhere
- **WHEN** central park management is requested from two different places in the application
- **THEN** both obtain the same instance

### Requirement: Current system date
At start-up the current system date SHALL be the local date. `SYSTEM DATE;<yyyy-MM-dd>` SHALL replace it for the rest of the execution, as many times as needed; every date-dependent operation SHALL use the current system date. An invalid date SHALL be rejected without changing the current date.

#### Scenario: Date changed
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** subsequent date-dependent checks treat 2027-04-15 as today

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;2027-13-40` or `SYSTEM DATE;15/04/2027` is processed
- **THEN** an error is reported and the current system date is unchanged

### Requirement: Current base ticket price
Each execution SHALL start with a base ticket price of 40.00 euros. `SYSTEM PRICE;<Price>` SHALL set a new base price that is a valid positive monetary amount; it SHALL apply only to tickets created afterwards, SHALL NOT change existing tickets and SHALL NOT be persisted between executions.

#### Scenario: New price applies only to new tickets
- **WHEN** a ticket is created, then `SYSTEM PRICE;45.50` is processed, then another ticket is created
- **THEN** the first ticket keeps price 40.00 and the second ticket costs 45.50

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-3` or `SYSTEM PRICE;abc` is processed
- **THEN** an error is reported and the base price is unchanged

#### Scenario: Price resets on restart
- **WHEN** the price was changed with `SYSTEM PRICE` and the application is started again
- **THEN** the base price is 40.00 euros
