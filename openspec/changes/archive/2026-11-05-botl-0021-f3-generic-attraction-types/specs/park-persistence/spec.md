# Spec Delta

## MODIFIED Requirements

### Requirement: Park state survives restarts
The application SHALL store in text files, inside its data directory, everything needed to rebuild groups and their members, individual tickets and their user, group tickets with their component tickets and (while the group exists) the purchasing group, attractions with their identifier, name, capacity and type (a persisted attraction without a type SHALL be rebuilt as GENERAL), and SpeedPasses and Special Passes with their identifier, price, associated individual ticket and consumed indicator. It SHALL rebuild that state at startup. Persistence SHALL NOT use automatic Java object serialization, databases, persistence frameworks or external libraries.

#### Scenario: Groups, tickets and attractions rebuilt
- **GIVEN** a run that created group `G01` with members, an individual ticket `T001`, a group ticket `TG01` for `G01`, and attraction `A01` with capacity 20, and then ended
- **WHEN** the application starts again with the same data directory
- **THEN** `G01` has the same members, `T001` has the same user, date and price, `TG01` has the same date, total, components and group, and `A01` has the same name and capacity

#### Scenario: Changes persist after each command
- **GIVEN** a run in which `GROUP CREATE;G02;Amigos` was processed
- **WHEN** the run ends without `EXIT`, at the end of its command file, and the application starts again
- **THEN** group `G02` exists

#### Scenario: Passes rebuilt
- **GIVEN** a run that bought SpeedPass `SP01:1` and Special Pass `PE01` for ticket `T001`, with `PE01` consumed
- **WHEN** the application starts again
- **THEN** both passes exist, linked to `T001` with the same prices, and `PE01` is still consumed

#### Scenario: Attraction types rebuilt
- **GIVEN** a SPECIAL attraction `A04`, and an attractions file line `A09;Antigua;5` without a type
- **WHEN** the application starts again
- **THEN** `A04` is SPECIAL and `A09` is GENERAL
