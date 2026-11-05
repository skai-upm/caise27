# Spec Delta

## ADDED Requirements

### Requirement: Passes deleted with their tickets
A valid `TICKET DELETE` SHALL also delete every pass attached to that ticket, and a valid `GROUPTICKET DELETE` SHALL delete the passes attached to each of its individual tickets, then those individual tickets, then the group ticket; a deletion rejected by the ticket rules SHALL delete no pass.

#### Scenario: Individual cascade
- **GIVEN** system date `2026-10-01` and ticket `T001` dated `2026-10-15` with SpeedPasses `SP01-1` … `SP01-10` and Special Pass `SPC1`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** `T001` and its eleven passes no longer exist, now or after restart, and the output reports the passes deleted

#### Scenario: Group cascade
- **GIVEN** future group ticket `TG01` whose constituent tickets have passes
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** the passes of every constituent ticket, the constituent tickets and `TG01` are all removed

#### Scenario: Rejected deletion keeps passes
- **GIVEN** ticket `T001` dated on the system date with passes
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** it is rejected and the ticket and all its passes remain

### Requirement: No partial cascade
If storing any part of a cascading deletion fails, the system SHALL report an error and leave tickets, group tickets and passes, in memory and in the files, as they were before the command.

#### Scenario: Ticket file cannot be written
- **GIVEN** the passes were removed but writing the tickets file fails
- **WHEN** the deletion ends
- **THEN** the passes are restored, the ticket still exists, and an error is printed

### Requirement: Pass and attraction state restored on restart
After a restart the system SHALL restore every pass with its identifier, price, associated individual ticket and consumed status, and every attraction with its type.

#### Scenario: Consumed pass after restart
- **GIVEN** SpeedPass `SP01-1` was consumed at a SPEED entry and attraction `A04` is SPECIAL
- **WHEN** the application restarts normally
- **THEN** `SP01-1` exists, consumed, with its price and ticket, and `A04` is SPECIAL
