# Spec Delta

## MODIFIED Requirements

### Requirement: TICKET DELETE command
`TICKET DELETE;<id>` SHALL delete an individual ticket only when its date is strictly later than the system date, deleting together with it all its SpeedPasses and its Special Pass. Tickets dated on or before the system date, unknown identifiers, and tickets that are constituents of a group ticket SHALL be rejected leaving the ticket and its passes unchanged.

#### Scenario: Only future tickets are refunded
- **WHEN** the system date is 2026-10-15 and tickets dated 2026-10-14, 2026-10-15 and 2026-10-16 exist and `TICKET DELETE` is executed for each
- **THEN** only the ticket dated 2026-10-16 is deleted

#### Scenario: Constituent ticket cannot be deleted alone
- **WHEN** `TICKET DELETE` names a ticket that belongs to a group ticket
- **THEN** an error is reported and the group ticket is unchanged

#### Scenario: Cascade to passes
- **WHEN** future ticket `T001` with SpeedPasses and a Special Pass is deleted and the application restarts
- **THEN** neither `T001` nor any of its passes exists
