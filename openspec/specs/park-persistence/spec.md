# park-persistence Specification

## Purpose

Preserves groups, tickets, and attractions between runs in plain text files, keeps historical ticket records, and provides a disposable temporary storage mode for isolated runs.

## Requirements

### Requirement: Park data files
Groups (with members), tickets, attractions (with type), and passes (type, identifier, ticket, stored price, consumed flag) SHALL be stored as text files in the active data directory, next to `users.txt`, without object serialization, databases, or persistence libraries. Every successful change SHALL be written before its confirmation is printed. If writing fails, an error SHALL be printed and the in-memory state SHALL remain as before the command.

#### Scenario: Park state survives restart
- **WHEN** users, a group with members, an individual ticket, a group ticket, and an attraction are created, and the application is restarted
- **THEN** all of them exist again with the same members, owners, dates, prices, group ticket composition, and group reference

#### Scenario: Passes and types survive restart
- **WHEN** SpeedPasses (one consumed) and a Special Pass are bought, a `SPECIAL` attraction is created, and the application restarts
- **THEN** the passes exist with the same tickets, prices, and consumed flags, and the attraction is still `SPECIAL`

#### Scenario: Untyped attraction lines load as general
- **WHEN** `attractions.txt` contains a Delivery 2 line `A01;Dragon Rojo;20`
- **THEN** `A01` is loaded as a `GENERAL` attraction

### Requirement: Attractions start empty
The occupants of attractions SHALL NOT be persisted. Every run SHALL start with all attractions empty.

#### Scenario: Occupants not restored
- **WHEN** Ana is inside `A01` when the application exits and the application starts again
- **THEN** `A01` exists and has no occupants

### Requirement: Historical records preserved
Historical tickets SHALL be kept and reloaded even when their user or purchasing group no longer exists. They SHALL keep their identifier, date, price, holder identity, and composition, and a group ticket whose group was deleted SHALL have no group reference.

#### Scenario: Ticket of a deleted user survives restart
- **WHEN** a user with only historical tickets is deleted and the application restarts
- **THEN** those tickets still exist with their identifier, date, price, and the user's DNI and email

#### Scenario: Group ticket of a deleted group survives restart
- **WHEN** a group whose only group tickets are historical is deleted and the application restarts
- **THEN** those group tickets still exist with their composition and no group reference

### Requirement: Temporary persistence mode
With `--fly`, the application SHALL create `tmp/<yyyyMMdd_HHmmss>/` in the working directory, adding a suffix if that name exists. All data files SHALL be read and written only there, through the same storage implementation used in normal runs. The run SHALL start with no data, and normal-mode data files SHALL remain untouched.

#### Scenario: Normal data untouched
- **WHEN** the working directory contains `users.txt` and a `--fly` run creates users, groups, tickets, and attractions
- **THEN** the working directory's data files are unchanged after the run

### Requirement: Temporary directory cleanup
At the end of a `--fly` run, the temporary directory and all its files SHALL be deleted. This applies when `EXIT` is processed, when a command file or standard input ends, and when the command file cannot be read.

#### Scenario: Cleanup after command file
- **WHEN** `java -jar target/amusement-park.jar --fly input_fase2.txt` finishes processing the file
- **THEN** no directory created by that run remains under `tmp/`
