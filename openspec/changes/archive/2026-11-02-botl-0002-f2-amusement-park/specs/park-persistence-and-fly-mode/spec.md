# Spec Delta

## Purpose

Defines persistence of the extended park state in text files and the `--fly` temporary execution mode that isolates a run's data (F2-C08).

## ADDED Requirements

### Requirement: Extended park persistence
The application SHALL store in text files, and reconstruct at start-up, users, groups with their members, individual tickets with their user, group tickets with their constituent tickets and (while it exists) their group, and attractions. Java object serialization, databases, persistence frameworks and external libraries SHALL NOT be used. Every successful state-changing command SHALL be persisted; a rejected command SHALL leave stored data unchanged.

#### Scenario: Relationships reconstructed
- **WHEN** groups, individual tickets, group tickets and attractions are created and the application is restarted
- **THEN** they exist again with the same members, users, compositions, group references, dates and prices

#### Scenario: Historical ticket of a deleted user
- **WHEN** a user with only historical tickets is deleted and the application is restarted
- **THEN** those tickets are reconstructed with their identifier, date, price and user information

### Requirement: Occupants are not persisted
The users inside attractions SHALL NOT be persisted; at the start of every execution all attractions SHALL be empty.

#### Scenario: Empty after restart
- **WHEN** users are inside an attraction and the application is restarted
- **THEN** the attraction exists with no occupants

### Requirement: Temporary fly mode
When `--fly` is the first argument, optionally followed by a command file path, the application SHALL create a new directory `tmp/<yyyyMMdd_HHmmss>/` identified by a start timestamp, configure all repositories (users, groups, tickets, attractions) to use only files inside it through path configuration of the same repositories, and delete that directory and its files when execution ends, both on `EXIT` and when the command file or input ends. Data outside that directory SHALL NOT be modified. Without `--fly`, argument behaviour SHALL remain as in Delivery 1.

#### Scenario: Fly run leaves no data
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly input_fase2.txt` runs a file that creates users, groups, tickets and attractions
- **THEN** after it ends the timestamped temporary directory no longer exists and the normal data files are unchanged

#### Scenario: Interactive fly with EXIT
- **WHEN** the application is started with `--fly`, a user is created interactively and `EXIT` is entered
- **THEN** the temporary directory is deleted and the user does not exist in a later normal run
