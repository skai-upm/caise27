# Spec Delta

## MODIFIED Requirements

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
