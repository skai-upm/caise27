# Spec Delta

## MODIFIED Requirements

### Requirement: Delete group ticket
`GROUPTICKET DELETE;id` SHALL delete a group ticket only when its date is later than the system date: first the passes of each of its individual tickets, then those individual tickets, and finally the group ticket, printing `OK GROUP TICKET DELETED`. A rejected deletion SHALL leave the group ticket, its individual tickets and their passes unchanged.

#### Scenario: Future group ticket
- **WHEN** TG01 is dated 2026-10-15 and the system date is 2026-10-14
- **THEN** `GROUPTICKET DELETE;TG01` removes TG01 and its individual tickets

#### Scenario: Current or past group ticket
- **WHEN** TG01 is dated 2026-10-15 and the system date is 2026-10-15
- **THEN** an `ERROR` result is printed and nothing is removed

#### Scenario: Passes of members deleted in cascade
- **WHEN** future TG01 is deleted while its individual tickets have SpeedPasses or Special Passes
- **THEN** those passes, the individual tickets and TG01 no longer exist, also after a restart
