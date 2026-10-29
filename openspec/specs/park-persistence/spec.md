# park-persistence Specification

## Purpose

Defines how groups, tickets and attractions are preserved between runs in text files, and the temporary --fly execution mode that isolates a run's data.

## Requirements

### Requirement: Park state persisted in text files
Groups with their users, individual tickets with their user, group tickets with their composition and (while it exists) their group, attractions with their type, and SpeedPasses and Special Passes with their ticket, stored price and consumed state SHALL be stored in text files and reconstructed at start-up. Attraction lines without a type MUST be read as GENERAL. Java object serialization, databases, persistence frameworks and external persistence libraries MUST NOT be used.

#### Scenario: State survives restart
- **WHEN** a group, an individual ticket, a group ticket and an attraction are created and the application is restarted
- **THEN** all four are reconstructed with the same identifiers, dates, prices, members, compositions and group reference

#### Scenario: Passes and types survive restart
- **WHEN** a SPECIAL attraction, a consumed Special Pass and unconsumed SpeedPasses exist and the application restarts
- **THEN** the attraction type, the passes, their tickets, prices and consumed states are reconstructed

#### Scenario: Delivery 2 attraction file
- **WHEN** `attractions.txt` contains `A01;Dragon Rojo;20`
- **THEN** A01 is loaded as a GENERAL attraction

### Requirement: Occupants not persisted
The users inside attractions SHALL NOT be persisted; every attraction MUST be empty when a new execution starts.

#### Scenario: Attractions empty after restart
- **WHEN** Ana was inside A01 when the previous execution ended
- **THEN** A01 has no occupants in the next execution

### Requirement: Historical records survive deletions
Historical tickets SHALL be reconstructable after their user or group has been deleted: tickets keep the user data needed for the record, and a group ticket whose group was deleted keeps identifier, date, price and composition with an empty group reference.

#### Scenario: Ticket of deleted user reloaded
- **WHEN** Ana has only a historical ticket, is deleted, and the application restarts
- **THEN** the historical ticket is reloaded with Ana's DNI and email, and Ana is not an active user

### Requirement: Temporary execution mode
When `--fly` is the first argument, the application SHALL create `tmp/<yyyyMMdd_HHmmss>/`, point all repositories at files inside it, and delete it with all its files when execution ends through EXIT or end of input. It MAY be followed by a command file path. Without `--fly`, argument behaviour is unchanged.

#### Scenario: Interactive fly mode
- **WHEN** `java -jar target/amusement-park.jar --fly` runs and receives `USER CREATE;...` then `EXIT`
- **THEN** the user is created in the temporary directory, the normal data files are unchanged and the temporary directory no longer exists afterwards

#### Scenario: File fly mode
- **WHEN** `java -jar target/amusement-park.jar --fly input_fase2.txt` runs to the end of the file
- **THEN** the commands are processed and no data generated during the run remains

#### Scenario: Same repositories
- **WHEN** the code is inspected
- **THEN** temporary mode uses the same repository classes as normal mode, configured with different paths
