# park-persistence Specification

## Purpose

Defines how groups, tickets and attractions are kept between executions in text files, how historical records survive deletions, and how the temporary `--fly` mode isolates an execution's data.

## Requirements

### Requirement: Park data reconstruction
Groups with their members, individual tickets with their user, group tickets with their composing tickets and (while it exists) their group, and attractions SHALL be stored in text files without Java object serialization and reconstructed at the next start.

#### Scenario: Restart
- **WHEN** a group, an individual ticket, a group ticket and an attraction are created and the application is restarted
- **THEN** all of them exist with the same relationships, identifiers, dates and prices

### Requirement: Attractions start empty
Attraction occupants SHALL NOT be persisted; at the start of every execution all attractions SHALL be empty.

#### Scenario: Occupants not restored
- **WHEN** users are inside A01 when an execution ends and the application is started again
- **THEN** A01 has no occupants

### Requirement: Historical records survive deletions
Historical tickets of a deleted user or group SHALL be kept with their identifier, date, price and composition, and SHALL be reconstructed after a restart even though the user or group no longer exists.

#### Scenario: Deleted user's past ticket
- **WHEN** Ana has only a past ticket, is deleted, and the application is restarted
- **THEN** that ticket still exists with its identifier, date, price and Ana's identifying data

#### Scenario: Deleted group's past ticket
- **WHEN** a group with only past group tickets is deleted and the application is restarted
- **THEN** its group tickets still exist with their composition, without a group reference

### Requirement: Temporary execution mode
When `--fly` is the first argument (optionally followed by a command file path) the application SHALL store all data (users, groups, tickets, attractions) only in a newly created timestamped directory under `tmp/`, using the same repositories as normal mode, and SHALL delete that directory and its files when execution ends through `EXIT`, end of input, or the end of a command file; without `--fly` the behaviour SHALL be unchanged.

#### Scenario: Fly with command file
- **WHEN** `java -jar <jar> --fly input.txt` creates users and tickets
- **THEN** the commands behave normally and, after the application ends, neither the temporary directory nor changes to the normal data files remain

#### Scenario: Fly interactive
- **WHEN** `java -jar <jar> --fly` is run and `EXIT` is typed
- **THEN** the temporary directory is removed
