# park-persistence-and-fly-mode Specification

## Purpose

Preserves the whole park state between executions in text files and provides an isolated temporary execution mode that leaves permanent data untouched.

## Requirements

### Requirement: Park state persistence
Groups with their users, individual tickets with their user, group tickets with their composition and, while the group exists, their group reference, and attractions SHALL be stored in text files and reconstructed as objects and object references at start-up, without Java object serialization, databases, persistence frameworks or external libraries. Persistence SHALL remain separate from the domain classes and the main class.

#### Scenario: Restart reconstructs relationships
- **WHEN** groups, individual tickets, a group ticket and attractions are created and the application is started again
- **THEN** the same groups with the same members, the tickets with their users, the group ticket with its composition and group, and the attractions are available

### Requirement: Occupancy is not persisted
The users inside attractions SHALL NOT be persisted; every attraction SHALL be empty at the start of an execution.

#### Scenario: Attractions empty after restart
- **WHEN** users are inside an attraction and the application is started again
- **THEN** the attraction has no occupants

### Requirement: Historical tickets of deleted users and groups
Persistence SHALL keep the information needed to reconstruct historical tickets whose user or group no longer exists: identifier, date, price, composition and the user's identifying data. A deleted group SHALL be reconstructed as an empty group reference.

#### Scenario: Historical ticket of a deleted user
- **WHEN** a user with only historical tickets is deleted and the application is started again
- **THEN** those tickets still exist with their date, price and the user's DNI and email

### Requirement: Temporary --fly mode
When `--fly` is the first argument, the application SHALL create a temporary directory identified by a timestamp (for example `tmp/20260826_190315/`), configure every repository (users, groups, tickets, attractions) to use only files inside it, and delete that directory and its files when execution ends, both on `EXIT` and when a command file or standard input is exhausted. The same repositories SHALL be used as in normal mode, differing only in configured paths. `--fly` MAY be followed by a command file path. Without `--fly`, argument behavior SHALL remain as in Delivery 1.

#### Scenario: Interactive --fly
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly` runs, creates a user and ends with `EXIT`
- **THEN** the temporary directory no longer exists and the permanent user data is unchanged

#### Scenario: File --fly
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly input_fase2.txt` processes the whole file
- **THEN** the commands run against an initially empty temporary state, the temporary directory is removed and the permanent data is unchanged
