# Spec Delta

## Purpose

Lets visitors attach SpeedPasses and a Special Pass to individual tickets, with their own price and consumption state, persistence and refund rules.

## ADDED Requirements

### Requirement: Pass data
Each pass SHALL belong to one individual ticket (bought directly or owned by a group ticket) and SHALL have its own unique identifier, purchase price and consumed/unused state. Consumed passes SHALL remain recorded.

#### Scenario: Pass belongs to ticket
- **GIVEN** ticket `T001`
- **WHEN** a SpeedPass is bought for it
- **THEN** the pass is attached to `T001`, has its own identifier and price, and is unused

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;IdTicket;Cantidad` SHALL accept quantity 1 or 10 and create that many independent SpeedPasses for the individual ticket, each priced at 20% of the ticket's stored price, replying `OK SPEEDPASS CREATED -> <total>` followed by the pass identifiers. Other quantities, unknown tickets, group-ticket identifiers and tickets dated before the system date SHALL be rejected.

#### Scenario: Ten SpeedPasses
- **GIVEN** ticket `T001` with price 40.00 dated on or after the system date
- **WHEN** `SPEEDPASS CREATE;T001;10` is executed
- **THEN** ten distinct unused SpeedPasses priced 8.00 exist and the reply starts with `OK SPEEDPASS CREATED -> 80.00`

#### Scenario: Invalid quantity
- **WHEN** `SPEEDPASS CREATE;T001;5` is executed
- **THEN** the command is rejected and no pass is created

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;IdTicket` SHALL create one Special Pass priced at the ticket's stored price, replying `OK SPECIALPASS CREATED -> <precio>` followed by the pass identifier. A ticket that has ever acquired a Special Pass SHALL be rejected, even if that pass was consumed or refunded; unknown tickets, group-ticket identifiers and tickets dated before the system date SHALL be rejected.

#### Scenario: Only one Special Pass per ticket lifetime
- **GIVEN** ticket `T001` already acquired a Special Pass that has been consumed
- **WHEN** `SPECIALPASS CREATE;T001` is executed
- **THEN** the command is rejected

### Requirement: Pass refunds
`SPEEDPASS DELETE;IdPase` and `SPECIALPASS DELETE;IdPase` SHALL remove an unused pass of the corresponding kind only if its ticket date is strictly later than the system date. Consumed passes, passes of the other kind, unknown identifiers and passes whose ticket is dated today or earlier SHALL be rejected.

#### Scenario: Only unused future passes refundable
- **GIVEN** ticket `T001` dated after the system date with one unused and one consumed SpeedPass
- **WHEN** both are submitted to `SPEEDPASS DELETE`
- **THEN** only the unused pass is removed

#### Scenario: Ticket date not refundable
- **GIVEN** a pass whose ticket is dated on the system date
- **WHEN** its refund is requested
- **THEN** the command is rejected

### Requirement: Pass persistence
Passes SHALL be persisted and restored at startup with their identifier, price, ticket association and consumed state, together with whether each ticket has ever acquired a Special Pass.

#### Scenario: Passes survive restart
- **GIVEN** ticket `T001` with a consumed SpeedPass and an unused Special Pass
- **WHEN** the application restarts
- **THEN** both passes exist on `T001` with the same prices and states
