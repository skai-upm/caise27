# Spec Delta

## Purpose

Adds SpeedPasses and Special Passes attached to individual tickets, with prices, refunds, consumption status and persistence.

## ADDED Requirements

### Requirement: Buy SpeedPasses
`SPEEDPASS CREATE;IdEntrada;Cantidad` SHALL, for an existing individual ticket (bought individually or internal to a group ticket) dated on or after the system date, create 1 or 10 independent SpeedPasses identified `<IdEntrada>-SP<n>`, each storing its own price (20 % of the ticket's stored price) and an unused status, and SHALL report the amount charged. Any other quantity SHALL be rejected.

#### Scenario: Pack of ten
- **WHEN** T001 costs 40.00 and `SPEEDPASS CREATE;T001;10` is processed
- **THEN** ten unused SpeedPasses of 8.00 each exist for T001 and the answer reports `80.00`

#### Scenario: Single pass
- **WHEN** `SPEEDPASS CREATE;T001;1` is processed
- **THEN** one SpeedPass of 8.00 is created and `8.00` is reported

#### Scenario: Invalid quantity or ticket
- **WHEN** `SPEEDPASS CREATE;T001;5`, `SPEEDPASS CREATE;TG01;1` (a group ticket) or `SPEEDPASS CREATE;T404;1` is processed
- **THEN** an `ERROR` is reported and no pass is created

### Requirement: Buy a Special Pass
`SPECIALPASS CREATE;IdEntrada` SHALL create a Special Pass identified `<IdEntrada>-SPECIAL` priced at the ticket's stored price for an individual ticket dated on or after the system date. Only one Special Pass SHALL ever be purchased for a given individual ticket, even if it was later used or refunded.

#### Scenario: First Special Pass
- **WHEN** `SPECIALPASS CREATE;T001` is processed for a 40.00 ticket
- **THEN** a Special Pass of 40.00 is created and `40.00` is reported

#### Scenario: Second Special Pass
- **WHEN** T001 already had a Special Pass (unused, used or refunded) and `SPECIALPASS CREATE;T001` is processed
- **THEN** an `ERROR` is reported

### Requirement: Refund unused passes
`SPEEDPASS DELETE;IdPase` and `SPECIALPASS DELETE;IdPase` SHALL delete an unused pass of the corresponding type and report the refunded amount, only when the associated ticket's date is later than the system date.

#### Scenario: Refund
- **WHEN** the system date is 2026-10-01, T001 is dated 2026-10-15 and `SPEEDPASS DELETE;T001-SP1` is processed for an unused pass
- **THEN** the pass is deleted and its price is reported as refunded

#### Scenario: Consumed or current-day pass
- **WHEN** the pass was consumed, or the ticket is dated on or before the system date
- **THEN** an `ERROR` is reported and the pass remains

### Requirement: Consumed passes remain
Consuming a pass SHALL mark it as consumed without removing it.

#### Scenario: After use
- **WHEN** a SpeedPass is consumed at an attraction
- **THEN** it still exists with consumed status

### Requirement: Passes persist
Passes SHALL be stored in a `;`-separated text file with identifier, type, associated ticket, price and consumed status, and restored attached to their ticket when the application starts.

#### Scenario: Restart
- **WHEN** T001 has a consumed SpeedPass of 8.00 and the application restarts
- **THEN** the SpeedPass exists for T001 with price 8.00 and consumed status
