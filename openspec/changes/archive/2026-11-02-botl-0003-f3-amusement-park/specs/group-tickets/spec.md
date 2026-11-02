# Spec Delta

## MODIFIED Requirements

### Requirement: Delete group ticket
`GROUPTICKET DELETE;<groupTicketId>` SHALL delete, in this order, the passes of its constituent tickets, the constituent tickets, and the group ticket only when its date is strictly later than the system date; otherwise, or for an unknown identifier, it SHALL be rejected and nothing SHALL be deleted.

#### Scenario: All-or-nothing deletion
- **WHEN** the system date is 2026-10-10 and `GROUPTICKET DELETE;TG01` is executed for `TG01` dated 2026-10-15
- **THEN** `TG01` and all its constituent tickets are deleted

#### Scenario: Current group ticket not deletable
- **WHEN** the system date equals the date of `TG01` and `GROUPTICKET DELETE;TG01` is executed
- **THEN** an error is shown and `TG01` with all constituents remains

#### Scenario: Cascade without partial state
- **WHEN** future `TG01` has constituents with passes and `GROUPTICKET DELETE;TG01` succeeds
- **THEN** its passes, constituents, and the group ticket are gone; when the deletion is rejected, all of them remain
