# Spec Delta

## MODIFIED Requirements

### Requirement: Delete ticket command
`TICKET DELETE;IdTicket` SHALL delete an individual ticket only if its date is strictly later than the system date, deleting all its SpeedPasses and its Special Pass in the same operation. Tickets dated today or earlier, unknown identifiers, and tickets that belong to a group ticket SHALL be rejected, leaving the ticket and its passes unchanged.

#### Scenario: Future-only ticket refund
- **GIVEN** system date 2026-10-10 and tickets dated 2026-10-09, 2026-10-10 and 2026-10-11
- **WHEN** each is submitted to `TICKET DELETE`
- **THEN** only the ticket dated 2026-10-11 is deleted

#### Scenario: Constituent ticket cannot be deleted individually
- **GIVEN** a future group ticket
- **WHEN** `TICKET DELETE` names one of its constituent tickets
- **THEN** the command is rejected

#### Scenario: Passes cascade-deleted
- **GIVEN** a future ticket with SpeedPasses and a Special Pass
- **WHEN** it is deleted
- **THEN** none of its passes exists afterwards, also after a restart
