# Spec Delta

## MODIFIED Requirements

### Requirement: Delete future group ticket
`GROUPTICKET DELETE;id` SHALL delete a group ticket only when its date is strictly later than the system date, deleting first the passes of each internal ticket, then the internal tickets and finally the group ticket. A rejected or failed deletion SHALL leave no partial deletions.

#### Scenario: Future group ticket deleted
- **WHEN** the system date is 2026-10-01 and `GROUPTICKET DELETE;TG01` is processed for a ticket dated 2026-10-15
- **THEN** TG01 and its internal tickets are deleted

#### Scenario: Current group ticket kept
- **WHEN** the system date equals the group ticket date
- **THEN** an `ERROR` is reported and nothing is deleted

#### Scenario: Passes of internal tickets deleted
- **WHEN** an internal ticket of future TG01 has SpeedPasses and `GROUPTICKET DELETE;TG01` is processed
- **THEN** those passes, the internal tickets and TG01 no longer exist, also after a restart
