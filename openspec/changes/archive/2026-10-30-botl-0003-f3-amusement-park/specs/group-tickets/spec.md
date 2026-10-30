# Spec Delta

## MODIFIED Requirements

### Requirement: Delete group ticket
`GROUPTICKET DELETE;<id>` SHALL, only when the group ticket's date is strictly after the system date, delete the passes of its constituent tickets, then the constituent tickets, then the group ticket, as one operation; otherwise nothing SHALL be deleted.

#### Scenario: All-or-nothing deletion
- **GIVEN** system date 2026-10-01 and group ticket `TG01` dated 2026-10-15 with three constituents
- **WHEN** `GROUPTICKET DELETE;TG01` is executed
- **THEN** `TG01` and its three constituent tickets no longer exist

#### Scenario: Deletion rejected on the ticket date
- **GIVEN** system date equal to the date of `TG01`
- **WHEN** `GROUPTICKET DELETE;TG01` is executed
- **THEN** an error is reported and `TG01` with all constituents remains

#### Scenario: Constituent passes deleted
- **GIVEN** a future group ticket whose constituents have SpeedPasses
- **WHEN** it is deleted
- **THEN** those SpeedPasses no longer exist
