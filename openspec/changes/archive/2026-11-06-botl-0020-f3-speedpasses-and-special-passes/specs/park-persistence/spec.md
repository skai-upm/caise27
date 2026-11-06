# Spec Delta

## MODIFIED Requirements

### Requirement: Park state survives restarts
The system SHALL store in text files, and reconstruct at the next start, the groups with their members, the individual tickets with their user, date and price, the group tickets with their composed individual tickets, the SpeedPasses and Special Passes with their identifier, price, individual ticket and consumed flag, and the attractions with their identifier, name and capacity. Every change made by a successful command SHALL be stored before the next command runs.

#### Scenario: Restart restores groups, tickets and attractions
- **GIVEN** an execution that created group `G01` with members, individual ticket `T001`, group ticket `TG01` for `G01` and attraction `A01`, and then ended
- **WHEN** the application starts again with the same data directory
- **THEN** `G01` has the same members, `T001` has the same user, date and price, `TG01` has the same composition, prices and group `G01`, and `A01` has the same name and capacity

#### Scenario: Rejected commands do not alter stored data
- **GIVEN** stored state with group `G01`
- **WHEN** `GROUP CREATE;G01;Otro` is rejected and the application restarts
- **THEN** `G01` keeps its original name

#### Scenario: Passes survive a restart
- **GIVEN** SpeedPass `SP01:1` and a consumed Special Pass `PE01` for ticket `T001`
- **WHEN** the application restarts
- **THEN** both passes exist with the same price and ticket, and `PE01` is still consumed
