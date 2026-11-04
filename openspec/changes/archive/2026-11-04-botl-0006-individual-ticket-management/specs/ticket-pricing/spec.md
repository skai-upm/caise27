# Spec Delta

## Purpose

Defines the execution-wide current price applied to new individual tickets and the command to change it.

## ADDED Requirements

### Requirement: Current ticket price
The main controller SHALL hold one current individual ticket price for the execution, initialised to 40.00 at every start and not persisted; each new ticket SHALL capture the price current at its creation.

#### Scenario: Default price at startup
- **GIVEN** a new execution, even after a previous execution changed the price
- **WHEN** a ticket is created before any `SYSTEM PRICE` command
- **THEN** its price is 40.00

### Requirement: SYSTEM PRICE command
The system SHALL process `SYSTEM PRICE;Precio`, where `Precio` is a positive amount written as digits optionally followed by `.` and one or two decimals, setting the current ticket price; invalid values SHALL be rejected with an `ERROR:` response leaving the price unchanged.

#### Scenario: Change the price
- **WHEN** `SYSTEM PRICE;60.00` is processed and then a ticket is created
- **THEN** an `OK:` response is printed for the price change and the response for the ticket is `OK TICKET CREATED -> 60.00`

#### Scenario: Existing tickets keep their price
- **GIVEN** ticket `T001` was created at 40.00
- **WHEN** `SYSTEM PRICE;60.00` is processed
- **THEN** `T001` still has price 40.00

#### Scenario: Invalid price
- **WHEN** any of `SYSTEM PRICE;abc`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;0`, `SYSTEM PRICE;60,00`, `SYSTEM PRICE;60.001` or `SYSTEM PRICE` is processed
- **THEN** an `ERROR:` response is printed and the price is unchanged
