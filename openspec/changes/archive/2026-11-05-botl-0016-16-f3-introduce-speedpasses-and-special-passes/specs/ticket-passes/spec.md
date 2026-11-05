# Spec Delta

## Purpose

Lets visitors buy personal passes attached to individual tickets — SpeedPasses and a single Special Pass — with their own price and consumption status, refund them when allowed, and keep them across executions.

## ADDED Requirements

### Requirement: Pass data
Every pass SHALL record its identifier (unique among all passes), its price, its associated individual ticket and whether it has been consumed; passes SHALL be either SpeedPasses or Special Passes.

#### Scenario: New pass state
- **WHEN** a pass is purchased
- **THEN** it has its identifier, its price, a reference to its individual ticket and consumed status false

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<IdCompra>;<IdTicket>;<Cantidad>` SHALL accept only quantity `1` or `10` and create that many independent SpeedPasses with identifiers `<IdCompra>-1` to `<IdCompra>-<Cantidad>`, each priced at 20% of the associated ticket's stored price, and SHALL print the total charged; it SHALL be rejected without creating any pass when the quantity is not 1 or 10, the ticket is not an existing individual ticket (bought directly or part of a group ticket), the ticket date is before the system date, or any generated identifier already exists.

#### Scenario: Pack of ten
- **GIVEN** system date `2026-10-01` and ticket `T001` dated `2026-10-15` at `40.00`
- **WHEN** `SPEEDPASS CREATE;SP01;T001;10` is processed
- **THEN** passes `SP01-1` … `SP01-10` exist, each priced `8.00`, and the output reports a total of `80.00`

#### Scenario: Disallowed quantity
- **WHEN** `SPEEDPASS CREATE;SP02;T001;5` or `SPEEDPASS CREATE;SP02;T001;0` is processed
- **THEN** it is rejected and no pass is created

#### Scenario: Identifier conflict creates nothing
- **GIVEN** pass `SP03-7` exists
- **WHEN** `SPEEDPASS CREATE;SP03;T001;10` is processed
- **THEN** it is rejected and none of `SP03-1` … `SP03-10` other than the existing one exists

#### Scenario: Constituent ticket of a group purchase
- **GIVEN** group ticket `TG01` with constituent ticket `TG01-12345678Z` storing `40.00`
- **WHEN** `SPEEDPASS CREATE;SP04;TG01-12345678Z;1` is processed
- **THEN** pass `SP04-1` is created priced `8.00`

#### Scenario: Past or unknown ticket
- **WHEN** the ticket does not exist or its date is before the system date
- **THEN** the purchase is rejected

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<Id>;<IdTicket>` SHALL create one Special Pass priced at the associated ticket's stored price and print the amount charged, and SHALL be rejected when the ticket already has a Special Pass (consumed or not), the identifier is used by any pass, or the ticket is unknown or dated before the system date.

#### Scenario: One per ticket
- **GIVEN** ticket `T001` at `40.00`
- **WHEN** `SPECIALPASS CREATE;SPC1;T001` and then `SPECIALPASS CREATE;SPC2;T001` are processed
- **THEN** the first creates `SPC1` priced `40.00` and the second is rejected

#### Scenario: Consumed Special Pass still counts
- **GIVEN** `T001`'s Special Pass has been consumed
- **WHEN** another Special Pass is requested for `T001`
- **THEN** it is rejected

### Requirement: Pass refund
`SPEEDPASS DELETE;<IdPase>` and `SPECIALPASS DELETE;<IdPase>` SHALL refund (delete) an existing pass of the corresponding kind only if it is not consumed and its ticket date is strictly after the system date, printing the refunded amount; otherwise the pass SHALL be kept and the command rejected.

#### Scenario: Refund allowed
- **GIVEN** system date `2026-10-01`, unconsumed `SP01-3` on a ticket dated `2026-10-15`
- **WHEN** `SPEEDPASS DELETE;SP01-3` is processed
- **THEN** `SP01-3` is removed and `8.00` is reported as refunded

#### Scenario: Refund rejected
- **WHEN** the pass is consumed, its ticket is dated on the system date or earlier, the pass does not exist, or the identifier belongs to the other pass kind
- **THEN** the command is rejected and the pass is unchanged

### Requirement: Consumed passes retained
Consuming a pass SHALL only change its consumed status; consumed passes SHALL remain stored.

#### Scenario: Consumption keeps the record
- **WHEN** a pass is consumed and the application restarts
- **THEN** the pass still exists with consumed status true

### Requirement: Pass persistence
Passes SHALL be stored one per line in `passes.txt` as `SPEED|SPECIAL;Id;IdTicket;Precio;true|false`, rewritten after every successful purchase or refund, and restored on startup linked to their individual tickets; lines whose ticket can no longer be found SHALL be kept unchanged as history with their identifiers reserved.

#### Scenario: Restart restores passes
- **GIVEN** `SP01-1` … `SP01-10` and `SPC1` were purchased
- **WHEN** the application restarts
- **THEN** all eleven passes exist with their prices, tickets and consumed status
