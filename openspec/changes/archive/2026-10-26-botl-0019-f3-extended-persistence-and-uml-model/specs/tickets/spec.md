# Spec Delta

## MODIFIED Requirements

### Requirement: Delete a future ticket
The system SHALL delete a ticket with `TICKET DELETE;Id` only when its date is strictly later than the system date; otherwise it SHALL report an error and keep the ticket. Deleting a ticket SHALL also delete all its SpeedPasses and its Special Pass. A failed deletion SHALL leave the ticket and its passes unchanged.

#### Scenario: Future ticket
- **WHEN** the system date is 2026-10-01 and `TICKET DELETE;T001` is processed for a ticket dated 2026-10-15
- **THEN** the ticket is deleted

#### Scenario: Ticket for today or the past
- **WHEN** the system date equals or is after the ticket date and `TICKET DELETE;T001` is processed
- **THEN** an error is reported and the ticket remains

#### Scenario: Cascade to passes
- **WHEN** future ticket T001 has three SpeedPasses and a Special Pass and `TICKET DELETE;T001` is processed
- **THEN** T001 and its four passes no longer exist, also after a restart

## REMOVED Requirements

### Requirement: Tickets with passes are not deleted
**Reason**: Interim rule replaced by cascade deletion of passes.
**Migration**: `TICKET DELETE` and `GROUPTICKET DELETE` now delete the passes together with their tickets.
