# Spec Delta

## MODIFIED Requirements

### Requirement: Refund group ticket
`GROUPTICKET DELETE;<id>` SHALL delete, in this order, the personal passes of every individual ticket of its composition, then those individual tickets, and finally the group ticket, and respond `OK GROUP TICKET DELETED` only when its date is strictly later than the system date; otherwise the response is `ERROR TICKET NOT REFUNDABLE`. An unknown identifier is rejected with `ERROR TICKET NOT FOUND`. A failed deletion leaves the group ticket, its individual tickets and their passes unchanged.

#### Scenario: Future group ticket refunded
- **WHEN** the system date is 2026-10-01 and `GROUPTICKET DELETE;TG01` is processed for a group ticket dated 2026-10-15
- **THEN** the response is `OK GROUP TICKET DELETED`, its individual tickets no longer exist and the group is unlocked

#### Scenario: Passes of members deleted first
- **WHEN** TG01-1 has a SpeedPass and TG01-2 a Special Pass and `GROUPTICKET DELETE;TG01` succeeds
- **THEN** the passes, the individual tickets TG01-1 and TG01-2 and TG01 no longer exist, also after restarting

#### Scenario: Not refundable leaves everything
- **WHEN** the system date is the group ticket's date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** the response is `ERROR TICKET NOT REFUNDABLE` and the group ticket, its tickets and their passes are unchanged
