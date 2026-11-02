# Spec Delta

## MODIFIED Requirements

### Requirement: Delete individual ticket
`TICKET DELETE;<ticketId>` SHALL delete a standalone individual ticket, together with all its passes, only when its date is strictly later than the system date. A ticket dated today or earlier, an unknown identifier, or a ticket that is a constituent of a group ticket SHALL be rejected without changes.

#### Scenario: Future-only ticket refund
- **WHEN** the system date is 2026-10-15 and tickets dated 2026-10-14, 2026-10-15, and 2026-10-16 are each submitted to `TICKET DELETE`
- **THEN** only the 2026-10-16 ticket is deleted

#### Scenario: Constituent ticket
- **WHEN** `TICKET DELETE` names an individual ticket that belongs to a group ticket
- **THEN** an error is shown and the group ticket and all its constituents remain

#### Scenario: Cascade to passes
- **WHEN** future ticket `T001` has SpeedPasses and a Special Pass and `TICKET DELETE;T001` succeeds
- **THEN** the ticket and all its passes are removed and no longer appear after restart
