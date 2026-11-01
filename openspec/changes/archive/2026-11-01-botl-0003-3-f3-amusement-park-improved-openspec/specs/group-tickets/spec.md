# Spec Delta

## MODIFIED Requirements

### Requirement: Delete group ticket command
`GROUPTICKET DELETE;IdTicket` SHALL delete a group ticket only if its date is strictly later than the system date, removing first the passes of its constituent tickets, then the constituent tickets and finally the group ticket, persisted as one operation. Otherwise, or if persisting fails, the state SHALL remain unchanged with no partial deletion.

#### Scenario: All-or-nothing group-ticket deletion
- **GIVEN** a future group ticket with several constituent tickets
- **WHEN** it is deleted
- **THEN** the group ticket and all its constituents are removed

#### Scenario: Current group ticket not deletable
- **GIVEN** a group ticket dated on the system date
- **WHEN** `GROUPTICKET DELETE` names it
- **THEN** the command is rejected and all its tickets remain

#### Scenario: Constituent passes removed
- **GIVEN** a future group ticket whose constituent tickets have SpeedPasses
- **WHEN** it is deleted
- **THEN** those passes no longer exist, also after a restart
