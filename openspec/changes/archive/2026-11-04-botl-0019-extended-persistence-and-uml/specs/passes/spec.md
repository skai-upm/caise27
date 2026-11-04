# Spec Delta

## MODIFIED Requirements

### Requirement: Pass persistence
Passes SHALL be stored in a text file, one per line, as `kind;id;ticketId;price;consumed` (kind `SPEED` or `SPECIAL`), saved after each successful purchase, refund, consumption or cascade deletion, and restored at startup after the tickets and group tickets, linked to the loaded individual ticket (standalone or constituent) with that identifier, preserving identity, kind, stored price and consumed status.

#### Scenario: File contents
- **WHEN** `SPEEDPASS CREATE;SP1;T001;1` succeeds
- **THEN** the passes file contains `SPEED;SP1;T001;8.00;false`

#### Scenario: Reconstruction after restart
- **GIVEN** SpeedPass `SP1` (8.00, consumed) for standalone ticket `T001` and Special Pass `SX1` (60.00, unconsumed) for constituent ticket `TG01-1`
- **WHEN** the application restarts
- **THEN** both passes exist with the same kind, price and consumed status, linked to the same individual ticket objects as the reloaded tickets, so `SX1` can still be used at a SPECIAL attraction and `SP1` cannot be refunded

## ADDED Requirements

### Requirement: Atomic cascade deletion
When a ticket deletion that cascades to passes fails at any step, the system SHALL restore every removed element so that no partial deletion remains, and SHALL report an `ERROR`.

#### Scenario: Ticket storage failure
- **GIVEN** ticket `T001` with SpeedPass `SP1` and a tickets file that cannot be written
- **WHEN** `TICKET DELETE;T001` is processed with a valid future date
- **THEN** an `ERROR` message is produced, and both `T001` and `SP1` still exist in memory and in the passes file
