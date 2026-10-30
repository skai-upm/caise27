# park-persistence Specification

## Purpose

Keeps groups, tickets and attractions between runs in text files and provides a temporary mode whose data is discarded at the end of the run.

## Requirements

### Requirement: Persist park elements
Groups with their members, individual tickets with their user, group tickets with their composition and (while it exists) their group, attractions with their type, and SpeedPasses and Special Passes with their ticket, stored price and consumed state SHALL be stored in text files and reconstructed at start-up, without Java serialization, databases or external libraries other than BCrypt. Attraction occupants SHALL NOT be persisted; all attractions start empty. Attraction records without a type SHALL load as `GENERAL`.

#### Scenario: Reconstruct after restart
- **WHEN** groups, tickets and attractions are created and the application is restarted
- **THEN** they exist with the same members, compositions, prices and group references

#### Scenario: Attractions start empty
- **WHEN** users were inside an attraction when the application ended
- **THEN** after restart that attraction has no occupants

#### Scenario: Passes and types survive restart
- **WHEN** a SPECIAL attraction exists and a consumed Special Pass and unconsumed SpeedPasses exist, and the application restarts
- **THEN** the attraction is SPECIAL and the passes keep their ticket, price and consumed state

#### Scenario: Legacy attraction line
- **WHEN** the attractions file contains `A01;Dragon Rojo;20`
- **THEN** A01 loads as a GENERAL attraction

### Requirement: Historical tickets survive deletions
Historical tickets of deleted users and of deleted groups SHALL be kept, including after restart, with identifier, date, price and composition; the group reference of a deleted group's tickets SHALL be empty.

#### Scenario: Deleted user's historical ticket
- **WHEN** a user with only a past ticket is deleted and the application restarts
- **THEN** the ticket still exists with its price and the user's identity

### Requirement: Temporary persistence mode
When `--fly` is the first argument, optionally followed by a command file path, the application SHALL store every element only in a new directory `tmp/<yyyyMMdd_HHmmss>/` and delete that directory and its files when the run ends by `EXIT` or end of the command file. Without `--fly`, behaviour SHALL be as before.

#### Scenario: Fly with command file
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly input_fase2.txt` runs commands that create users and tickets
- **THEN** the normal data files are untouched and no temporary data remains afterwards

#### Scenario: Fly interactive
- **WHEN** the application starts with `--fly` and `EXIT` is typed
- **THEN** the temporary directory has been removed
