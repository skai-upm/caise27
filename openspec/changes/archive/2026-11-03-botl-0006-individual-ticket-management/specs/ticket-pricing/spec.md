# Spec Delta

## Purpose

Defines the execution-wide current ticket price used when tickets are created and how it can be changed.

## ADDED Requirements

### Requirement: Default price
The current ticket price SHALL be 40.00 at the start of every execution and SHALL NOT be persisted.

#### Scenario: New execution
- **GIVEN** a previous execution set the price to 60.00
- **WHEN** a new execution creates a ticket without `SYSTEM PRICE`
- **THEN** the ticket price is 40.00

### Requirement: Change the price
The command `SYSTEM PRICE;Precio` SHALL set the current ticket price to a non-negative decimal amount with at most two decimals (e.g. `60.00`); other values or a missing parameter SHALL be rejected without changing the price. Tickets created earlier SHALL keep the price they captured.

#### Scenario: New price applies to later tickets
- **GIVEN** ticket `T001` created at 40.00
- **WHEN** `SYSTEM PRICE;60.00` is processed and then `TICKET CREATE;T002;ana@correo.com;2026-10-20`
- **THEN** the response is `OK TICKET CREATED -> 60.00` and `T001` still has price 40.00

#### Scenario: Invalid price
- **WHEN** any of `SYSTEM PRICE;abc`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;10.999`, `SYSTEM PRICE` is processed
- **THEN** an error is reported and the current price is unchanged
