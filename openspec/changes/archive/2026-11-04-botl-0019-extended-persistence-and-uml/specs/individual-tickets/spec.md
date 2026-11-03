# Spec Delta

## MODIFIED Requirements

### Requirement: Ticket deletion
The command `TICKET DELETE;Id` SHALL delete the ticket, together with all its SpeedPasses and its Special Pass, only if its date is strictly later than the current system date; a ticket dated on the system date or earlier, or a nonexistent ticket, SHALL be rejected without deleting anything. If the deletion cannot be completed, neither the ticket nor any of its passes SHALL be deleted.

#### Scenario: Future ticket
- **GIVEN** system date 2026-10-01 and ticket `T001` dated 2026-10-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** the ticket is deleted

#### Scenario: Ticket for today cannot be deleted
- **GIVEN** system date 2026-10-15 and ticket `T001` dated 2026-10-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an error is reported and the ticket remains

#### Scenario: Past ticket cannot be deleted
- **GIVEN** system date 2026-10-16 and ticket `T001` dated 2026-10-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an error is reported and the ticket is kept as history

#### Scenario: Passes deleted with the ticket
- **GIVEN** future ticket `T001` with SpeedPasses `S1-1`, `S1-2` and Special Pass `SP1`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** `T001`, `S1-1`, `S1-2` and `SP1` no longer exist and none of them is reloaded after a restart

#### Scenario: Failed deletion is not partial
- **GIVEN** ticket `T001` with passes
- **WHEN** `TICKET DELETE;T001` is rejected or fails while storing
- **THEN** `T001` and all its passes still exist
