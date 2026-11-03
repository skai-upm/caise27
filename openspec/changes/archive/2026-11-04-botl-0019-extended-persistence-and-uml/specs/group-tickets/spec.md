# Spec Delta

## MODIFIED Requirements

### Requirement: Group ticket deletion
The command `GROUPTICKET DELETE;Id` SHALL, only if the group ticket's date is strictly later than the system date, delete first the passes associated with each of its constituent individual tickets, then those constituent tickets and finally the group ticket; otherwise, or if it does not exist, it SHALL be rejected without deleting anything. If the deletion cannot be completed, no pass, constituent ticket or group ticket SHALL be deleted.

#### Scenario: Future group ticket
- **GIVEN** system date 2026-10-01 and `TG001` dated 2026-10-15
- **WHEN** `GROUPTICKET DELETE;TG001` is processed
- **THEN** `TG001` and its constituent tickets no longer exist

#### Scenario: Current or past group ticket
- **GIVEN** `TG001` dated 2026-10-15 and system date 2026-10-15 or later
- **WHEN** `GROUPTICKET DELETE;TG001` is processed
- **THEN** an error is reported and the group ticket is kept

#### Scenario: Cascade through the composition
- **GIVEN** future `TG001` whose constituent tickets `TG001-1` and `TG001-2` have SpeedPasses and a Special Pass
- **WHEN** `GROUPTICKET DELETE;TG001` is processed
- **THEN** the passes, the constituent tickets and `TG001` no longer exist

#### Scenario: Failed group deletion is not partial
- **WHEN** `GROUPTICKET DELETE;TG001` is rejected or fails while storing
- **THEN** `TG001`, its constituent tickets and all their passes still exist
