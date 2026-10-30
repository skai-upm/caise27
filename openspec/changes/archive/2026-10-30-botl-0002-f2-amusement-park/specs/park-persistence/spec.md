# Spec Delta

## Purpose

Defines how groups, tickets and attractions are preserved between executions in text files, and the temporary `--fly` mode that isolates an execution's data.

## ADDED Requirements

### Requirement: Park state persisted in text files
Groups with their members, individual tickets with their user, group tickets with their composition and group reference, and attractions SHALL be stored in text files (no Java serialization, databases or libraries) and reconstructed at start-up with the same relationships.

#### Scenario: State survives restart
- **WHEN** users, a group with members, an individual ticket, a group ticket and an attraction are created and the application is restarted
- **THEN** all of them exist again with the same members, compositions, prices, dates and group reference

### Requirement: Attractions start empty
Attraction occupants SHALL NOT be persisted; every attraction SHALL be empty at the start of an execution.

#### Scenario: Occupants not restored
- **WHEN** a user is inside A01 when the application ends and it is started again
- **THEN** A01 has no occupants

### Requirement: Historical records survive deletions
Historical tickets of a deleted user, and historical group tickets of a deleted group, SHALL be reconstructed after restart with their identifier, date, price, composition and the former user's identifying data; the deleted group's reference stays null.

#### Scenario: Deleted group's historical ticket after restart
- **WHEN** a group with a past group ticket is deleted and the application restarts
- **THEN** the group ticket exists with its composition and price and no group

### Requirement: Temporary --fly mode
When `--fly` is the first argument, optionally followed by a command file path, the application SHALL create a new directory `tmp/<yyyyMMdd_HHmmss>/`, make every repository use files only inside it, and delete it with all its files when execution ends (EXIT, end of input, or end of file). Without `--fly` argument handling is unchanged.

#### Scenario: Fly with a command file leaves no data
- **WHEN** `java -jar target/amusement-park.jar --fly input.txt` creates users and tickets
- **THEN** the commands run normally, the permanent data files are not created or changed, and no `tmp/<timestamp>` directory remains

#### Scenario: Fly interactively
- **WHEN** `java -jar target/amusement-park.jar --fly` reads commands from standard input until EXIT
- **THEN** the commands run normally and the temporary directory is removed afterwards
