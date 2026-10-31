# Spec Delta

## Purpose

Lets holders of individual tickets buy SpeedPasses and a Special Pass for those tickets, tracks each pass's price and consumption, and allows refunds of unused passes under the ticket refund rules.

## ADDED Requirements

### Requirement: Pass model
Every pass SHALL belong to one individual ticket (stand-alone or group constituent) and SHALL have its own identifier, purchase price and consumption state (initially unused). SpeedPass identifiers SHALL be `<ticketId>-SPEED<n>` with `n` increasing per ticket; the Special Pass identifier SHALL be `<ticketId>-SPECIAL`.

#### Scenario: New pass state
- **WHEN** a SpeedPass is bought for `T001`
- **THEN** pass `T001-SPEED1` exists for `T001`, unused, with its price

### Requirement: SPEEDPASS CREATE command
`SPEEDPASS CREATE;<ticketId>;<quantity>` SHALL accept quantity `1` or `10` only and create that many independent SpeedPasses, each priced at 20% of the ticket's stored price, printing `OK SPEEDPASS CREATED -> <total>`. It SHALL be rejected for unknown tickets, group tickets, other quantities, or tickets dated before the system date.

#### Scenario: Pack of ten
- **WHEN** `T001` has price 40.00 and `SPEEDPASS CREATE;T001;10` is executed
- **THEN** ten distinct unused SpeedPasses priced 8.00 exist for `T001` and the output is `OK SPEEDPASS CREATED -> 80.00`

#### Scenario: Invalid quantity or ticket
- **WHEN** `SPEEDPASS CREATE;T001;5`, `SPEEDPASS CREATE;TG01;1` or `SPEEDPASS CREATE;NOPE;1` is executed
- **THEN** an error is reported and no pass is created

### Requirement: SPECIALPASS CREATE command
`SPECIALPASS CREATE;<ticketId>` SHALL create a Special Pass priced at the ticket's stored price and print `OK SPECIALPASS CREATED -> <price>`. An individual ticket SHALL receive at most one Special Pass in its lifetime: a second purchase SHALL be rejected even if the first pass was consumed or refunded. Unknown tickets, group tickets and tickets dated before the system date SHALL be rejected.

#### Scenario: Special Pass price
- **WHEN** `T001` has price 40.00 and `SPECIALPASS CREATE;T001` is executed
- **THEN** the output is `OK SPECIALPASS CREATED -> 40.00`

#### Scenario: Only one per lifetime
- **WHEN** `T001` already received a Special Pass, consumed or not, and `SPECIALPASS CREATE;T001` is executed again
- **THEN** an error is reported

### Requirement: Pass refunds
`SPEEDPASS DELETE;<passId>` and `SPECIALPASS DELETE;<passId>` SHALL delete an unused pass of the named kind only when its ticket's date is strictly later than the system date. Consumed passes, unknown identifiers, passes of the other kind and passes of current or past tickets SHALL be rejected. Consumed passes SHALL remain recorded.

#### Scenario: Refund unused future pass
- **WHEN** the system date is 2026-10-01, `T001` is dated 2026-10-15 and `SPEEDPASS DELETE;T001-SPEED1` is executed for an unused pass
- **THEN** the pass is deleted

#### Scenario: Consumed or current pass not refunded
- **WHEN** the pass was consumed, or its ticket is dated on or before the system date
- **THEN** an error is reported and the pass remains

### Requirement: Pass persistence
Passes SHALL be persisted after every change and restored at startup with identifier, kind, price, associated individual ticket and consumption state; the lifetime Special Pass restriction SHALL survive restarts.

#### Scenario: Restore passes
- **WHEN** passes are bought, one is consumed and the application restarts
- **THEN** the same passes exist with the same prices, tickets and consumption states
