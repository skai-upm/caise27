# Spec Delta

## Purpose

Adds personal passes — SpeedPasses and Special Passes — attached to individual tickets, with their prices, consumption state, purchase, refund and persistence.

## ADDED Requirements

### Requirement: Pass data
Each pass SHALL be an independent object with an identifier, a kind (SpeedPass or Special Pass), its own stored price, a reference to its associated individual ticket and a consumed status that starts as not consumed. Pass identifiers SHALL be unique among all passes.

#### Scenario: Pass fields
- **WHEN** a SpeedPass is bought for ticket `T001` priced 40.00
- **THEN** the pass records its identifier, price 8.00, ticket `T001` and consumed = false

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;IdCompra;IdTicket;Cantidad` SHALL buy SpeedPasses for an existing individual ticket (standalone or constituent of a group ticket). `Cantidad` SHALL be `1` or `10`. Quantity 1 creates one pass with identifier `IdCompra`; quantity 10 creates ten independent passes with identifiers `IdCompra-1` … `IdCompra-10`. Each SpeedPass SHALL cost 20% of the associated ticket's stored price, rounded to two decimals. The reply SHALL be `OK SPEEDPASS CREATED -> <total>`. If any identifier is already used, or the input is invalid, nothing SHALL be created.

#### Scenario: Single SpeedPass
- **GIVEN** ticket `T001` with stored price 40.00
- **WHEN** `SPEEDPASS CREATE;SP1;T001;1` is processed
- **THEN** the reply is `OK SPEEDPASS CREATED -> 8.00` and pass `SP1` exists

#### Scenario: Pack of ten
- **WHEN** `SPEEDPASS CREATE;PK;T001;10` is processed
- **THEN** the reply is `OK SPEEDPASS CREATED -> 80.00` and passes `PK-1` to `PK-10` exist, each priced 8.00 and unconsumed

#### Scenario: Constituent ticket
- **WHEN** `SPEEDPASS CREATE;SP2;TG01-1;1` is processed for a group ticket priced at 60.00 per member
- **THEN** the reply is `OK SPEEDPASS CREATED -> 12.00`

#### Scenario: Invalid purchases are atomic
- **WHEN** the quantity is `0`, `2`, `5` or `diez`, the ticket does not exist or is a group ticket id, the identifier is blank, or `PK-3` already exists and `SPEEDPASS CREATE;PK;T001;10` is processed
- **THEN** an `ERROR` message is produced and no pass is created

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;IdPase;IdTicket` SHALL buy a Special Pass for an existing individual ticket that has no existing Special Pass (consumed or not). It SHALL cost 100% of the ticket's stored price and reply `OK SPECIALPASS CREATED -> <price>`.

#### Scenario: Special Pass bought
- **WHEN** `SPECIALPASS CREATE;SX1;T001` is processed for a ticket priced 40.00
- **THEN** the reply is `OK SPECIALPASS CREATED -> 40.00`

#### Scenario: Only one per ticket, even when consumed
- **GIVEN** ticket `T001` has Special Pass `SX1`, consumed or not
- **WHEN** `SPECIALPASS CREATE;SX2;T001` is processed
- **THEN** an `ERROR` message is produced and no pass is created

### Requirement: Pass refund
`SPEEDPASS DELETE;IdPase` and `SPECIALPASS DELETE;IdPase` SHALL refund (remove) an unconsumed pass of that kind only when its associated ticket's date is strictly later than the current system date. Consumed passes, passes of the other kind, unknown identifiers and passes whose ticket is dated today or earlier SHALL be rejected.

#### Scenario: Refund of an unused pass
- **GIVEN** system date 2026-10-01 and unconsumed SpeedPass `SP1` for ticket `T001` dated 2026-10-15
- **WHEN** `SPEEDPASS DELETE;SP1` is processed
- **THEN** an `OK` message is produced and `SP1` no longer exists

#### Scenario: Refund not permitted
- **WHEN** the pass is consumed, the ticket is dated on or before the system date, the identifier is unknown, or `SPEEDPASS DELETE` names a Special Pass
- **THEN** an `ERROR` message is produced and the pass is kept

### Requirement: Consumed passes are kept
A consumed pass SHALL remain stored with consumed = true and SHALL NOT be removed when used.

#### Scenario: Consumed state survives restart
- **GIVEN** pass `SP1` was marked consumed
- **WHEN** the application restarts
- **THEN** `SP1` exists with consumed = true and cannot be refunded

### Requirement: Pass persistence
Passes SHALL be stored in a text file, one per line, as `kind;id;ticketId;price;consumed` (kind `SPEED` or `SPECIAL`), saved after each successful purchase or refund and restored at startup linked to the loaded individual ticket with that identifier.

#### Scenario: File contents
- **WHEN** `SPEEDPASS CREATE;SP1;T001;1` succeeds
- **THEN** the passes file contains `SPEED;SP1;T001;8.00;false`
