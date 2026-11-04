# passes Specification

## Purpose
Defines personal passes attached to individual tickets — SpeedPasses and Special Passes — with their prices, purchase and refund rules, consumption status and persistence.

## Requirements

### Requirement: Pass data
Each pass SHALL be an independent object with an identifier, the individual ticket it is attached to, its own price and a consumed status (initially not consumed); pass identifiers SHALL be unique among all passes.

#### Scenario: Fields of a new pass
- **GIVEN** ticket `T001` stored at 40.00
- **WHEN** `SPECIALPASS CREATE;S01;T001` is processed
- **THEN** pass `S01` exists attached to `T001` with price 40.00 and not consumed

### Requirement: SpeedPass purchase
The system SHALL process `SPEEDPASS CREATE;<compra>;<ticket>;<cantidad>` where `<cantidad>` is `1` or `10`, creating that many independent SpeedPasses identified `<compra>-1` … `<compra>-<cantidad>`, each priced at 20% of the ticket's stored price, and answering `OK: SPEEDPASS CREATED -> <total>` with the total of the created passes; any other quantity, an unknown ticket, a ticket dated before the system date, or an already used derived identifier SHALL be rejected with `ERROR:` and create no pass.

#### Scenario: Single SpeedPass
- **GIVEN** ticket `T001` stored at 40.00 dated on or after the system date
- **WHEN** `SPEEDPASS CREATE;P01;T001;1` is processed
- **THEN** the response is `OK: SPEEDPASS CREATED -> 8.00` and pass `P01-1` exists with price 8.00

#### Scenario: Pack of ten
- **WHEN** `SPEEDPASS CREATE;P02;T001;10` is processed
- **THEN** passes `P02-1` … `P02-10` exist, each priced 8.00, and the response is `OK: SPEEDPASS CREATED -> 80.00`

#### Scenario: Disallowed quantity
- **WHEN** `SPEEDPASS CREATE;P03;T001;2`, `…;0` or `…;diez` is processed
- **THEN** an `ERROR:` response is printed and no pass is created

#### Scenario: Identifier conflict leaves no partial purchase
- **GIVEN** pass `P04-3` already exists
- **WHEN** `SPEEDPASS CREATE;P04;T001;10` is processed
- **THEN** an `ERROR:` response is printed and none of `P04-1`, `P04-2`, `P04-4` … `P04-10` is created

### Requirement: Special Pass purchase
The system SHALL process `SPECIALPASS CREATE;<id>;<ticket>`, creating a Special Pass priced at 100% of the ticket's stored price and answering `OK: SPECIALPASS CREATED -> <precio>`, unless the ticket already has a Special Pass (consumed or not), the identifier is used, the ticket does not exist or is dated before the system date.

#### Scenario: Second Special Pass rejected
- **GIVEN** ticket `T001` has Special Pass `S01`, even if consumed
- **WHEN** `SPECIALPASS CREATE;S02;T001` is processed
- **THEN** an `ERROR:` response is printed

### Requirement: Passes on group ticket constituents
Passes SHALL be purchasable for constituent tickets of group tickets, priced on the constituent's stored (discounted) price.

#### Scenario: Discounted constituent
- **GIVEN** `TG01` for 3 members at individual price 40.00 (10% discount), so `TG01-1` is stored at 36.00
- **WHEN** `SPEEDPASS CREATE;P05;TG01-1;1` and `SPECIALPASS CREATE;S05;TG01-1` are processed
- **THEN** the responses are `OK: SPEEDPASS CREATED -> 7.20` and `OK: SPECIALPASS CREATED -> 36.00`

### Requirement: Pass refund
The system SHALL process `SPEEDPASS DELETE;<id>` and `SPECIALPASS DELETE;<id>`, removing the pass of that kind only if it is not consumed and its ticket's date is strictly after the system date; consumed passes SHALL never be removed or refunded.

#### Scenario: Refund allowed
- **GIVEN** unconsumed SpeedPass `P01-1` on a ticket dated after the system date
- **WHEN** `SPEEDPASS DELETE;P01-1` is processed
- **THEN** an `OK:` response is printed and `P01-1` no longer exists

#### Scenario: Refund rejected
- **WHEN** the pass is consumed, its ticket is dated today or in the past, the identifier is unknown, or the identifier belongs to the other kind of pass
- **THEN** an `ERROR:` response is printed and the pass is kept

#### Scenario: Special Pass after a refund
- **GIVEN** Special Pass `S01` of `T001` was refunded
- **WHEN** `SPECIALPASS CREATE;S02;T001` is processed
- **THEN** it is created, because the refunded purchase was cancelled

### Requirement: Ticket deletion removes its passes
When an individual ticket is deleted — directly or as a constituent of a deleted group ticket — its passes SHALL be deleted with it: for `TICKET DELETE` the ticket's passes first and then the ticket; for `GROUPTICKET DELETE` the passes of every constituent first, then the constituent tickets, then the group ticket. The deletion SHALL be validated completely before anything is removed, and if any step fails no partial deletion SHALL remain.

#### Scenario: Group ticket deleted with passes
- **GIVEN** future `TG01` and SpeedPass `P05-1` on `TG01-1`
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** `P05-1` no longer exists

#### Scenario: Individual cascade
- **GIVEN** future ticket `T001` with SpeedPasses `P02-1` … `P02-10` and Special Pass `S01`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** `T001` and its eleven passes no longer exist, also after a restart

#### Scenario: Failed validation deletes nothing
- **GIVEN** ticket `T001` dated today with passes
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an `ERROR:` response is printed and the ticket and all its passes are kept

#### Scenario: Failed write leaves no partial cascade
- **GIVEN** a future ticket with passes and the ticket file cannot be written
- **WHEN** the ticket deletion is attempted
- **THEN** an `ERROR:` response is printed and both the ticket and its passes still exist

### Requirement: Pass persistence
Passes SHALL be stored in `passes.txt` (one per line `Tipo;Identificador;Ticket;Precio;Consumido`, with `Tipo` `SPEED` or `SPECIAL` and `Consumido` `true`/`false`) after every change and restored at startup attached to the loaded tickets, keeping their consumed status.

#### Scenario: Restart
- **GIVEN** passes `P02-1` … `P02-10` and `S01` on `T001`, with `S01` consumed, and the application stopped
- **WHEN** it starts again
- **THEN** the eleven passes exist with the same prices, ticket and consumed status, and `S01` cannot be refunded nor a second Special Pass bought for `T001`

### Requirement: Pass commands in HELP
`HELP` SHALL include `SPEEDPASS CREATE;Identificador;Ticket;Cantidad`, `SPEEDPASS DELETE;Identificador`, `SPECIALPASS CREATE;Identificador;Ticket` and `SPECIALPASS DELETE;Identificador`.

#### Scenario: Help shows pass commands
- **WHEN** `HELP` is processed
- **THEN** the four lines are present
