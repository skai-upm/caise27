# Spec Delta

## ADDED Requirements

### Requirement: Atomic cascading deletion
`TICKET DELETE` and `GROUPTICKET DELETE` SHALL check every rule before changing anything and SHALL either delete the ticket with everything that depends on it (passes; for a group ticket also its internal tickets) or leave all of it unchanged, also when storing the result fails.

#### Scenario: Group ticket cascade
- **WHEN** future group ticket `TG01` with internal tickets `TG01-1` (with SpeedPass `TG01-1-S1`) and `TG01-2` is deleted
- **THEN** `TG01-1-S1`, `TG01-1`, `TG01-2` and `TG01` no longer exist

#### Scenario: Rejected deletion
- **WHEN** `GROUPTICKET DELETE;TG01` is received on the ticket date
- **THEN** an error is printed and `TG01`, its internal tickets and their passes are unchanged

#### Scenario: Storage failure
- **WHEN** a valid deletion is attempted but the tickets cannot be saved
- **THEN** an error is reported and the ticket, its internal tickets and their passes are still present
