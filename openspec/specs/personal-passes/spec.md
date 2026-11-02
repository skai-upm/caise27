# personal-passes Specification

## Purpose

Adds personal passes attached to individual tickets—SpeedPasses for faster access and one Special Pass for special attractions—with their own price, consumption state, refund rules, and persistence.

## Requirements

### Requirement: Buy SpeedPasses
`SPEEDPASS CREATE;<idEntrada>;<cantidad>` SHALL accept quantity `1` or `10` for an existing individual ticket (standalone or constituent) dated on or after the system date, create that many independent SpeedPasses identified `<idEntrada>-S<n>` (numbered consecutively per ticket), each unused and priced at 20% of the ticket's stored price rounded half-up to cents, and respond `OK SPEEDPASS CREATED -> <importe>` with the total charged. Other quantities, unknown or group tickets, and past tickets SHALL be rejected without creating passes.

#### Scenario: Pack of ten
- **WHEN** `T001` was sold at 40.00 and `SPEEDPASS CREATE;T001;10` runs
- **THEN** the output is `OK SPEEDPASS CREATED -> 80.00` and passes `T001-S1` … `T001-S10` exist, each unused with price 8.00

#### Scenario: Invalid quantity
- **WHEN** `SPEEDPASS CREATE;T001;5` runs
- **THEN** an error is shown and no pass is created

### Requirement: Buy Special Pass
`SPECIALPASS CREATE;<idEntrada>` SHALL create one Special Pass `<idEntrada>-SPECIAL` for an existing individual ticket dated on or after the system date, priced at the ticket's stored price, and respond `OK SPECIALPASS CREATED -> <importe>`. A ticket that has ever had a Special Pass — even if consumed or refunded — SHALL be rejected.

#### Scenario: One per ticket lifetime
- **WHEN** `SPECIALPASS CREATE;T001` succeeded and the pass was later consumed or refunded, and `SPECIALPASS CREATE;T001` runs again
- **THEN** an error is shown and no new pass is created

#### Scenario: Price equals ticket
- **WHEN** `T001` was sold at 60.00 and `SPECIALPASS CREATE;T001` runs
- **THEN** the output is `OK SPECIALPASS CREATED -> 60.00`

### Requirement: Refund passes
`SPEEDPASS DELETE;<idPase>` and `SPECIALPASS DELETE;<idPase>` SHALL remove an unused pass of the matching kind only when its ticket is dated strictly after the system date; consumed passes, passes of today's or past tickets, and unknown identifiers SHALL be rejected without changes.

#### Scenario: Unused versus consumed
- **WHEN** the ticket is in the future, `T001-S1` is unused and `T001-S2` is consumed
- **THEN** `SPEEDPASS DELETE;T001-S1` succeeds and `SPEEDPASS DELETE;T001-S2` is rejected

#### Scenario: Ticket not refundable
- **WHEN** the ticket is dated on the system date
- **THEN** refunding any of its passes is rejected

### Requirement: Consumed passes are kept
Consuming a pass SHALL mark it consumed and keep it with its ticket.

#### Scenario: Consumed pass remains
- **WHEN** a SpeedPass is consumed on entry
- **THEN** the pass still exists with consumed status

### Requirement: Pass persistence
Passes SHALL be persisted with identifier, kind, ticket, price, and consumed status, together with each ticket's Special Pass history, and restored at startup attached to their tickets.

#### Scenario: Restart restores passes
- **WHEN** passes were bought and one was consumed, and the application restarts
- **THEN** every pass is attached to the same ticket with the same price and status, and the ticket still rejects a second Special Pass
