# Spec Delta

## Purpose

Lets users attach personal passes to individual tickets — SpeedPasses for priority access and one Special Pass for special attractions — with prices, consumption state, refunds and persistence.

## ADDED Requirements

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<idEntrada>;<cantidad>` SHALL buy 1 or 10 SpeedPasses for an existing individual ticket (bought directly or part of a group ticket) dated on or after the system date. Each SpeedPass SHALL be an independent object with its own identifier `<idEntrada>-SP<n>`, a price equal to 20% of the price stored in the ticket, and an unconsumed status. The output SHALL be `OK SPEEDPASS CREATED -> <importe> [<ids>]`.

#### Scenario: Buy one
- **WHEN** `SPEEDPASS CREATE;T001;1` is processed for T001 priced 40.00
- **THEN** one SpeedPass priced 8.00 is created and `OK SPEEDPASS CREATED -> 8.00 [T001-SP1]` is printed

#### Scenario: Buy a pack of ten
- **WHEN** `SPEEDPASS CREATE;T001;10` is processed for T001 priced 40.00
- **THEN** ten independent SpeedPasses are created and the amount charged is 80.00

#### Scenario: Invalid quantity or ticket
- **WHEN** the quantity is not 1 or 10, the ticket is a group ticket, does not exist, or is dated before the system date
- **THEN** an `ERROR` result is printed and no pass is created

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<idEntrada>` SHALL buy a Special Pass `<idEntrada>-SPECIAL` for an existing individual ticket dated on or after the system date, priced at the ticket's stored price, and print `OK SPECIALPASS CREATED -> <importe>`. A ticket SHALL have at most one Special Pass, even after it has been consumed.

#### Scenario: Buy special pass
- **WHEN** `SPECIALPASS CREATE;T001` is processed for T001 priced 40.00
- **THEN** a Special Pass priced 40.00 is created

#### Scenario: Second special pass
- **WHEN** T001 already has a Special Pass, consumed or not
- **THEN** `SPECIALPASS CREATE;T001` prints an `ERROR` result

### Requirement: Pass refund
`SPEEDPASS DELETE;<idSpeedPass>` and `SPECIALPASS DELETE;<idEntrada>` SHALL refund (delete) an unused pass only when the associated ticket's date is later than the system date, printing `OK SPEEDPASS DELETED -> <importe>` or `OK SPECIALPASS DELETED -> <importe>`. Consumed passes SHALL NOT be refunded. After a Special Pass is refunded, a new one MAY be bought for the same ticket.

#### Scenario: Refund unused pass
- **WHEN** T001 is dated 2026-10-15, the system date is 2026-10-14 and T001-SP1 is unused
- **THEN** `SPEEDPASS DELETE;T001-SP1` deletes it and reports the refunded amount

#### Scenario: Refund not permitted
- **WHEN** the pass is consumed, or the system date is on or after the ticket date
- **THEN** an `ERROR` result is printed and the pass remains

### Requirement: Consumed passes are kept
Passes SHALL keep their consumption status; consuming a pass SHALL NOT remove it.

#### Scenario: Pass after use
- **WHEN** a SpeedPass is consumed on attraction entry
- **THEN** it still exists with status consumed

### Requirement: Pass persistence
Passes SHALL be stored with identifier, type, associated individual ticket, price and consumption status, and restored at startup attached to their tickets.

#### Scenario: Passes survive restart
- **WHEN** SpeedPasses and a Special Pass exist, one of them consumed, and the application restarts
- **THEN** all passes exist with the same ticket, price and consumption status
