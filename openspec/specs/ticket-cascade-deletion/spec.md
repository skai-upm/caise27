# ticket-cascade-deletion Specification

## Purpose

Keeps tickets and their passes consistent when tickets are deleted: a valid deletion removes everything the ticket owns, and a failed one removes nothing.

## Requirements

### Requirement: Individual ticket deletion cascades to its passes
A valid `TICKET DELETE` (the ticket exists, is an individual ticket bought with `TICKET CREATE` and is dated later than the system date) SHALL delete all SpeedPasses and the Special Pass associated with that ticket, and then the ticket, persisting both changes.

#### Scenario: Ticket with passes
- **GIVEN** future ticket `T001` has SpeedPasses `SP1-1`, `SP1-2` and Special Pass `VIP1`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** the result starts with `OK`, `T001` and its three passes no longer exist, also after a restart, and passes of other tickets are untouched

### Requirement: Group ticket deletion cascades in order
A valid `GROUPTICKET DELETE` (the group ticket exists and is dated later than the system date) SHALL delete, in this order, the passes associated with each individual ticket of its composition, then those individual tickets, and finally the group ticket, persisting all changes.

#### Scenario: Group ticket with member passes
- **GIVEN** future group ticket `TG01` whose member tickets `TG01-12345678Z` and `TG01-87654321X` have SpeedPasses and a Special Pass
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** the result starts with `OK`, and the member passes, the member tickets and `TG01` no longer exist, also after a restart

### Requirement: No partial cascade
A deletion that is not valid (unknown ticket, wrong kind, ticket dated today or earlier) SHALL be rejected before anything is removed, and a deletion whose storage fails part-way SHALL restore everything already removed, so that after any failed deletion the ticket, its constituents and all their passes still exist unchanged.

#### Scenario: Ticket for today
- **GIVEN** `T001` has passes and is dated on the system date
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** the result starts with `ERROR` and `T001` and all its passes are kept

#### Scenario: Storage failure during the cascade
- **GIVEN** the passes were removed but saving the ticket file fails
- **WHEN** the deletion ends
- **THEN** the removed passes are restored in memory and in the passes file, and the command reports `ERROR`
