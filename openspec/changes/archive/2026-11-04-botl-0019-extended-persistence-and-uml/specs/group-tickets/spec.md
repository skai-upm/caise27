# Spec Delta

## MODIFIED Requirements

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;IdTicket` SHALL delete, in this order, the passes associated with each constituent ticket, the constituent tickets and finally the group ticket, only when its date is strictly later than the current system date; a group ticket dated on or before the system date, or an unknown identifier, SHALL be rejected.

#### Scenario: Future group ticket
- **GIVEN** `TG001` dated 2026-10-15 and system date 2026-10-01
- **WHEN** `GROUPTICKET DELETE;TG001` is processed
- **THEN** an `OK` message is produced, the group ticket and its constituent tickets no longer exist, and the group is unlocked

#### Scenario: Current, past or unknown group ticket
- **WHEN** the system date is on or after the ticket date, or the identifier does not exist
- **THEN** an `ERROR` message is produced and nothing is deleted

#### Scenario: Cascade order
- **GIVEN** future group ticket `TG01` whose constituents `TG01-1` and `TG01-2` have SpeedPasses and a Special Pass
- **WHEN** `GROUPTICKET DELETE;TG01` succeeds
- **THEN** the constituents' passes, the constituent tickets and the group ticket no longer exist, also after a restart

#### Scenario: Failed group deletion leaves everything
- **GIVEN** the group tickets file cannot be written
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** an `ERROR` message is produced and `TG01`, its constituents and their passes all still exist
