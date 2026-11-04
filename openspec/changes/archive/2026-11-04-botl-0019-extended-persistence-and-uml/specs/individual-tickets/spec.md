# Spec Delta

## MODIFIED Requirements

### Requirement: Ticket deletion
`TICKET DELETE;Id` SHALL delete a standalone individual ticket only when its date is strictly later than the current system date; a ticket dated on or before the system date, an unknown identifier, or a constituent ticket of a group ticket SHALL be rejected. A valid deletion SHALL first delete all SpeedPasses and the Special Pass associated with the ticket, then the ticket.

#### Scenario: Future ticket
- **GIVEN** system date 2026-10-01 and ticket `T001` dated 2026-10-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an `OK` message is produced and the ticket no longer exists

#### Scenario: Ticket for today or the past
- **GIVEN** ticket `T001` dated 2026-10-15
- **WHEN** the system date is 2026-10-15 or 2026-10-16 and `TICKET DELETE;T001` is processed
- **THEN** an `ERROR` message is produced and the ticket is kept

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed
- **THEN** an `ERROR` message is produced

#### Scenario: Constituent ticket
- **GIVEN** group ticket `TG01` with constituent ticket `TG01-1`
- **WHEN** `TICKET DELETE;TG01-1` is processed
- **THEN** an `ERROR` message is produced and the group ticket keeps all its constituents

#### Scenario: Passes deleted with the ticket
- **GIVEN** future ticket `T001` with SpeedPasses `PK-1` to `PK-10` and Special Pass `SX1`
- **WHEN** `TICKET DELETE;T001` succeeds
- **THEN** `T001` and all eleven passes no longer exist, also after a restart

#### Scenario: Rejected deletion keeps passes
- **GIVEN** ticket `T001` dated today with SpeedPass `SP1`
- **WHEN** `TICKET DELETE;T001` is rejected
- **THEN** `SP1` still exists
