# Spec Delta

## Purpose

Preserves groups, tickets and attractions with their relationships between runs, and offers an isolated temporary `--fly` mode that leaves permanent data untouched.

## ADDED Requirements

### Requirement: Park data persistence
The application SHALL store in text files, and reconstruct on start-up, groups and their users, individual tickets and their user, group tickets with their composed tickets and (while it exists) their group, and attractions, without Java serialization, databases, persistence frameworks or external libraries.

#### Scenario: Restart reconstructs relationships
- **WHEN** groups, individual tickets, group tickets and attractions are created and the application restarts
- **THEN** they are reconstructed with the same members, holders, compositions, group references, dates and prices

### Requirement: Occupants are not persisted
Attraction occupants SHALL NOT be persisted; every attraction SHALL start empty in a new execution.

#### Scenario: Attractions start empty
- **WHEN** users are inside an attraction and the application restarts
- **THEN** the attraction exists with no occupants

### Requirement: Historical records survive deletions
Historical tickets SHALL remain reconstructible after their user or group is deleted, keeping identifier, date, price and composition; a deleted group's reference SHALL be empty.

#### Scenario: Deleted user's historical ticket
- **WHEN** a user with only historical tickets is deleted and the application restarts
- **THEN** those tickets are still present with their holder's identifying data

### Requirement: Temporary persistence mode
With `--fly` as the first argument, optionally followed by a command file, every repository SHALL use only files in a new timestamped directory such as `tmp/20260826_190315/`, through the same repository implementations configured by path; the directory SHALL be deleted when execution ends through `EXIT` or end of input.

#### Scenario: Fly run leaves no data
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly input_fase2.txt` creates users and tickets and finishes
- **THEN** the temporary directory is removed and normal persistent files are unchanged

#### Scenario: Interactive fly run
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly` is used interactively and `EXIT` is entered
- **THEN** the temporary directory is removed

### Requirement: Normal argument behaviour unchanged
Without `--fly`, the application SHALL behave exactly as in Delivery 1: no argument means interactive mode and a first argument is a command file.

#### Scenario: Normal file mode
- **WHEN** `java -jar target/nombre-aplicacion.jar input_fase2.txt` runs
- **THEN** commands are processed against the permanent files
