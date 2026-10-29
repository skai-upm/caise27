# park-persistence Specification

## Purpose

Defines how groups, tickets and attractions are kept in text files between runs alongside users, and the temporary `--fly` mode that isolates an execution's data in a disposable directory.

## Requirements

### Requirement: Park state survives restarts
Groups with their members, individual tickets with their user, group tickets with their composition and (while it exists) their group, and attractions SHALL be stored in text files and reconstructed at start-up, without Java object serialization, databases, frameworks or external libraries.

#### Scenario: Reload park state
- **WHEN** users, a group with members, an individual ticket, a group ticket and an attraction are created, and the application is restarted
- **THEN** all of them exist again with the same relationships, identifiers, dates and prices

### Requirement: Occupants not persisted
The occupants of attractions SHALL NOT be persisted. Every attraction SHALL start empty in a new execution.

#### Scenario: Attractions empty after restart
- **WHEN** a user is inside A01 and the application is restarted
- **THEN** A01 has no occupants

### Requirement: Historical records preserved
Historical tickets SHALL be reconstructed after a restart even when their user or group has since been deleted. A ticket whose group was deleted SHALL keep its identifier, date, price and composition with no group reference.

#### Scenario: Deleted user's historical ticket
- **WHEN** a user with only a historical ticket is deleted and the application is restarted
- **THEN** the historical ticket still exists with its date, price and the deleted user's identity

### Requirement: Temporary persistence mode
When `--fly` is the first argument, the application SHALL create a new timestamped directory `tmp/<yyyyMMdd_HHmmss>/`, point every repository at files inside it, and delete it with all its files when execution ends (on `EXIT` or at the end of the input). The repositories SHALL be the same as in normal mode, differing only in configured paths. Normal-mode data SHALL NOT be read or modified.

#### Scenario: Fly with command file
- **WHEN** `java -jar target/amusement-park.jar --fly input_fase2.txt` runs commands that create users and tickets
- **THEN** the commands run normally, the normal data files are untouched, and no `tmp/<timestamp>/` directory remains afterwards

#### Scenario: Fly interactive
- **WHEN** `java -jar target/amusement-park.jar --fly` is run and `EXIT` is typed
- **THEN** the temporary directory is deleted

### Requirement: Attraction types and passes persisted
Each attraction's type SHALL be persisted, and attraction records without a type SHALL load as GENERAL. Every SpeedPass and Special Pass SHALL be persisted with its identifier, individual ticket, stored price and consumed state, including consumed passes, while its ticket exists. Passes SHALL be reconstructed at start-up. `--fly` mode SHALL cover these elements through the same repositories with different paths.

#### Scenario: Reload passes and types
- **WHEN** a SPECIAL attraction, a SpeedPass package with one consumed pass, and a Special Pass exist, and the application is restarted
- **THEN** the attraction is SPECIAL, and every pass exists with the same ticket, price and consumed state

#### Scenario: Legacy attraction line
- **WHEN** the attractions file contains `A01;Dragon Rojo;20`
- **THEN** A01 loads as a GENERAL attraction
