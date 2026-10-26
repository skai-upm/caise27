# personal-passes Specification

## Purpose

Lets visitors buy SpeedPasses and a Special Pass attached to an individual ticket, priced from the ticket, persisted with their status, and refundable while the ticket date allows it.

## Requirements

### Requirement: Buy SpeedPasses
The command `SPEEDPASS CREATE;IdTicket;Cantidad` SHALL accept only quantities `1` and `10` and SHALL create that many independent SpeedPasses for the individual ticket (standalone or internal to a group ticket), identified `<IdTicket>-S<n>` with `n` continuing the ticket's SpeedPass numbering. Each SpeedPass SHALL cost 20% of the price stored in the ticket (rounded to two decimals) and start AVAILABLE. The response SHALL be `OK SPEEDPASS CREATED -> <importe>` with the total charged, followed by the created identifiers. The ticket SHALL exist, be individual and not be dated before the system date; otherwise an error SHALL be reported and nothing created.

#### Scenario: Pack of ten
- **WHEN** ticket `T001` cost 40.00 and `SPEEDPASS CREATE;T001;10` is received
- **THEN** SpeedPasses `T001-S1` … `T001-S10` exist, each priced 8.00 and AVAILABLE, and the response starts with `OK SPEEDPASS CREATED -> 80.00`

#### Scenario: Single pass continues numbering
- **WHEN** `T001` already has `T001-S1` … `T001-S10` and `SPEEDPASS CREATE;T001;1` is received
- **THEN** `T001-S11` is created and the response starts with `OK SPEEDPASS CREATED -> 8.00`

#### Scenario: Invalid quantity or ticket
- **WHEN** `SPEEDPASS CREATE;T001;5`, `SPEEDPASS CREATE;T999;1`, `SPEEDPASS CREATE;TG01;1` (a group ticket) or a pass for a ticket dated before the system date is requested
- **THEN** an error is printed and no pass is created

### Requirement: Buy a Special Pass
The command `SPECIALPASS CREATE;IdTicket` SHALL create the Special Pass `<IdTicket>-SPECIAL` for an individual ticket, priced as the ticket's stored price, starting AVAILABLE, and SHALL answer `OK SPECIALPASS CREATED -> <importe>`. A ticket SHALL get at most one Special Pass during its lifetime: a second purchase SHALL be rejected even if the first was consumed or refunded. The same ticket conditions as for SpeedPasses apply.

#### Scenario: Special pass bought
- **WHEN** `SPECIALPASS CREATE;T001` is received for `T001` priced 40.00
- **THEN** `T001-SPECIAL` exists, priced 40.00, and `OK SPECIALPASS CREATED -> 40.00` is printed

#### Scenario: Second special pass
- **WHEN** `T001-SPECIAL` exists in any status and `SPECIALPASS CREATE;T001` is received
- **THEN** an error is printed and no new pass is created

### Requirement: Pass status
Every pass SHALL store its identifier, price and status: AVAILABLE, CONSUMED or REFUNDED. A consumed pass SHALL remain in the system with status CONSUMED.

#### Scenario: Consumed pass kept
- **WHEN** a pass is consumed
- **THEN** it still exists with status CONSUMED and cannot be refunded

### Requirement: Refund passes
The commands `SPEEDPASS DELETE;IdPase` and `SPECIALPASS DELETE;IdPase` SHALL refund a pass of the corresponding type only if it is AVAILABLE and its ticket date is strictly after the system date; the pass SHALL then have status REFUNDED and the refunded amount SHALL be printed. Otherwise an error SHALL be reported and the pass unchanged.

#### Scenario: Refund allowed
- **WHEN** the system date is 2026-10-01, `T001` is dated 2026-10-15 and `SPEEDPASS DELETE;T001-S1` is received
- **THEN** `T001-S1` becomes REFUNDED and `8.00` is reported as refunded

#### Scenario: Refund not allowed on the ticket date
- **WHEN** the system date is 2026-10-15 and `SPECIALPASS DELETE;T001-SPECIAL` is received
- **THEN** an error is printed and the pass stays AVAILABLE

#### Scenario: Wrong type or unknown pass
- **WHEN** `SPECIALPASS DELETE;T001-S1` or `SPEEDPASS DELETE;X-S9` is received
- **THEN** an error is printed

### Requirement: Pass persistence
Passes SHALL be stored in a text file with their type, identifier, ticket, price and status, and restored at startup attached to their individual ticket (standalone or internal to a group ticket) with the same price and status, including CONSUMED. Deleting a ticket SHALL first delete its SpeedPasses and Special Pass; deleting a group ticket SHALL delete the passes of each internal ticket, then the internal tickets, then the group ticket.

#### Scenario: Restart restores passes
- **WHEN** `T001` has `T001-S1` (CONSUMED) and `T001-SPECIAL` (REFUNDED) and the application restarts
- **THEN** both passes exist with the same prices and statuses, and `SPECIALPASS CREATE;T001` is still rejected

#### Scenario: Internal ticket passes restored
- **WHEN** internal ticket `TG01-1` has SpeedPass `TG01-1-S1` priced 8.00 and CONSUMED and the application restarts
- **THEN** `TG01-1-S1` is attached to `TG01-1` with price 8.00 and status CONSUMED

#### Scenario: Cascade on ticket deletion
- **WHEN** future ticket `T001` with SpeedPasses and a Special Pass is deleted with `TICKET DELETE;T001`
- **THEN** none of its passes remains in the system or in the pass file
