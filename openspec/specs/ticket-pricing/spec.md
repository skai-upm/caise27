# ticket-pricing Specification

## Purpose

Defines the internal ticket price used for new purchases, how it is initialized in each execution and how it is changed by command.

## Requirements

### Requirement: Default price per execution
Every execution SHALL start with an internal ticket price of `40.00`; the price SHALL NOT be persisted.

#### Scenario: Price resets on restart
- **GIVEN** an execution processed `SYSTEM PRICE;60.00` and ended
- **WHEN** a new execution creates a ticket
- **THEN** the output is `OK TICKET CREATED -> 40.00`

### Requirement: SYSTEM PRICE command
`SYSTEM PRICE;<Precio>` SHALL set the internal price used by tickets created afterwards when the value is a non-negative decimal number with digits and at most one decimal point with up to two decimals; otherwise it SHALL be rejected and the price unchanged. Tickets created before the change SHALL keep their captured price.

#### Scenario: New price applies to later tickets only
- **GIVEN** ticket `T001` created at `40.00`
- **WHEN** `SYSTEM PRICE;60.00` is processed and `TICKET CREATE;T002;ana@correo.com;2026-10-20` is processed
- **THEN** the output is `OK TICKET CREATED -> 60.00` and `T001` still has price `40.00`

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc`, `SYSTEM PRICE;60,00`, `SYSTEM PRICE;1.234`, `SYSTEM PRICE;1e3` or `SYSTEM PRICE` is processed
- **THEN** it is rejected and the price is unchanged
