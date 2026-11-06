# park-persistence Specification

## Purpose
Keeps the Amusement Park state (groups, tickets and attractions with their relationships) in text files so that it is reconstructed when the application starts again, preserves historical tickets as fiscal records, and supports an isolated temporary storage mode.

## Requirements

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

### Requirement: Attraction occupants are not persisted
The system SHALL NOT store the users inside attractions. At the start of every execution all attractions SHALL be empty.

#### Scenario: Attractions start empty
- **GIVEN** a user was inside attraction `A01` when the previous execution ended
- **WHEN** the application starts again
- **THEN** `ATTRACTION LIST;A01` shows no users

### Requirement: Fiscal records of historical tickets are preserved
The system SHALL keep, and reconstruct after a restart, historical tickets whose user or group no longer exists, with their identifier, date, price, user email and composition. A group ticket whose group was deleted SHALL be reconstructed without a group reference.

#### Scenario: Group deleted before restart
- **GIVEN** historical group ticket `TG01` whose group `G01` was deleted
- **WHEN** the application restarts
- **THEN** `TG01` exists with its date, price and composition and has no group reference
- **AND** creating a new group `G01` does not link it to `TG01`

### Requirement: Temporary persistence mode
The system SHALL, when `--fly` is the first argument, create a new directory `tmp/<yyyyMMdd_HHmmss>/` and make every repository use only files inside it for that execution, through the same repositories configured with that path. It SHALL delete that directory and all its files when the execution ends, either through EXIT or when the input ends normally. Data stored in normal mode SHALL be neither read nor modified.

#### Scenario: Fly run leaves no trace
- **GIVEN** normal data containing group `G01`
- **WHEN** `java -jar target/amusement-park.jar --fly input.txt` runs a file that creates group `G02` and ends with `EXIT`
- **THEN** during the run `G01` does not exist and `G02` can be created
- **AND** after the run the temporary directory no longer exists and the normal data still contains only `G01`

#### Scenario: Interactive fly run cleans up at end of input
- **WHEN** `java -jar target/amusement-park.jar --fly` is run and standard input ends without EXIT
- **THEN** the temporary directory is deleted

### Requirement: Unreadable stored data is handled in a controlled manner
The system SHALL report stored data lines that cannot be interpreted, skip them, and continue starting with the rest of the data, instead of terminating abnormally.

#### Scenario: Corrupt line in a data file
- **GIVEN** a data file containing a malformed line among valid ones
- **WHEN** the application starts
- **THEN** a warning about the skipped line is displayed and the valid records are available

### Requirement: Attraction type is persisted
The system SHALL store each attraction's type and restore it at startup. A stored attraction without a type SHALL be loaded as a GENERAL attraction.

#### Scenario: Typed attraction survives restart
- **GIVEN** SPECIAL attraction `A04` was created
- **WHEN** the application restarts
- **THEN** `A04` is still SPECIAL

#### Scenario: Data from Delivery 2
- **GIVEN** a stored attraction line `A01;Dragon Rojo;20` without a type
- **WHEN** the application starts
- **THEN** `A01` is a GENERAL attraction with capacity 20
