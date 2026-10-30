# Spec Delta

## MODIFIED Requirements

### Requirement: Delete individual ticket
`TICKET DELETE;<id>` SHALL delete a standalone individual ticket only when its date is strictly after the system date, together with all its passes. Tickets dated today or earlier, unknown identifiers, and tickets that belong to a group ticket SHALL be rejected without deleting anything.

#### Scenario: Only future tickets can be deleted
- **GIVEN** system date 2026-10-10 and tickets dated 2026-10-09, 2026-10-10 and 2026-10-11
- **WHEN** each is submitted to `TICKET DELETE`
- **THEN** only the ticket dated 2026-10-11 is deleted

#### Scenario: Passes deleted with the ticket
- **GIVEN** a future ticket with SpeedPasses and a Special Pass
- **WHEN** it is deleted
- **THEN** its passes are deleted too
