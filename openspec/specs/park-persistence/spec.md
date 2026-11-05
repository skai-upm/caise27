# park-persistence Specification

## Purpose
Defines how the park state (groups, tickets and attractions with their relationships) is stored in text files and rebuilt when the application starts again.

## Requirements

### Requirement: Park state survives restarts
The application SHALL store in text files, inside its data directory, everything needed to rebuild groups and their members, individual tickets and their user, group tickets with their component tickets and (while the group exists) the purchasing group, attractions with their identifier, name and capacity, and SpeedPasses and Special Passes with their identifier, price, associated individual ticket and consumed indicator. It SHALL rebuild that state at startup. Persistence SHALL NOT use automatic Java object serialization, databases, persistence frameworks or external libraries.

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

### Requirement: Occupants are not persisted
The users inside attractions SHALL NOT be persisted. At the start of every run, all attractions SHALL be empty.

#### Scenario: Attractions start empty
- **GIVEN** a run that ended with users inside `A01`
- **WHEN** the application starts again
- **THEN** `A01` exists with no occupants

### Requirement: Historical records survive deleted groups and users
Rebuilding SHALL keep historical tickets as fiscal records. A group ticket whose group was deleted SHALL be rebuilt with no group reference but with its identifier, date, price and composition. A ticket SHALL keep the identity (e-mail) of its user even if that user no longer exists, and a group created later with the same identifier SHALL NOT be linked to it.

#### Scenario: Group deleted before restart
- **GIVEN** historical group ticket `TG01` whose group `G01` was deleted
- **WHEN** the application restarts
- **THEN** `TG01` exists with no group reference and with its original date, price and components

#### Scenario: Same identifier reused after deletion
- **GIVEN** the previous state and a new group created with identifier `G01` before the restart
- **WHEN** the application restarts
- **THEN** `TG01` is still not linked to any group

### Requirement: Controlled handling of unreadable persisted data
If a persisted line can't be interpreted, the application SHALL report a warning, ignore that line and continue starting up with the remaining data. A missing data file SHALL mean that no elements of that type exist yet.

#### Scenario: First run
- **GIVEN** an empty data directory
- **WHEN** the application starts
- **THEN** no error is shown and there are no groups, tickets or attractions

#### Scenario: Corrupted line
- **GIVEN** an attractions file with a malformed line followed by a valid one
- **WHEN** the application starts
- **THEN** a warning is shown and the valid attraction is loaded
