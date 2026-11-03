# Spec Delta

## Purpose

Extends text-file persistence to groups, tickets and attractions with their relationships, keeps occupancy transient, and adds the isolated `--fly` temporary execution mode (source F2-C08, sections 13 and 13.1).

## ADDED Requirements

### Requirement: Persist park elements and relationships
Groups with their members, individual tickets with their user, group tickets with their composed tickets and (while it exists) their group, and attractions SHALL be stored in text files and reconstructed as object relationships at start-up, without automatic Java serialization, databases, persistence frameworks or external libraries.

#### Scenario: Relationships reconstructed after restart
- **WHEN** users, a group with members, an individual ticket, a group ticket and an attraction are created and the application is restarted
- **THEN** all of them exist again with the same members, holders, composition, prices, dates and group reference

#### Scenario: Historical tickets of deleted users and groups survive restart
- **WHEN** a user and a group whose tickets are all historical are deleted and the application is restarted
- **THEN** the historical tickets are reconstructed with their identifier, date, price and holder information, and the group ticket has no group reference

### Requirement: Occupancy is not persisted
The users inside each attraction SHALL NOT be persisted; every attraction SHALL be empty at the start of an execution.

#### Scenario: Attractions start empty
- **WHEN** users are inside attractions when the application exits and it is started again
- **THEN** every attraction has no users inside

### Requirement: Temporary fly mode
When `--fly` is the first argument, optionally followed by a command file path, the application SHALL create a timestamped directory `tmp/<yyyyMMdd_HHmmss>/`, make all repositories use only files inside it through path configuration, and delete it with all its files when execution ends through `EXIT` or the end of input.

#### Scenario: Fly mode with a command file leaves no data
- **WHEN** `java -jar <jar> --fly input_fase2.txt` processes a file that creates users, groups, tickets and attractions
- **THEN** the commands take effect during the run, the temporary directory no longer exists afterwards, and the normal data files are unchanged

#### Scenario: Fly mode interactively
- **WHEN** the application is started with `--fly` only and receives commands followed by `EXIT` on standard input
- **THEN** the commands are processed against empty temporary data and the temporary directory is removed at exit

### Requirement: Argument handling without fly is unchanged
Without `--fly`, the application SHALL behave exactly as in Delivery 1: no arguments selects interactive mode and a first argument selects file mode with permanent persistence.

#### Scenario: Normal file mode keeps permanent data
- **WHEN** `java -jar <jar> input_fase2.txt` is run
- **THEN** the resulting state is stored in the normal data files and is available in the next execution
