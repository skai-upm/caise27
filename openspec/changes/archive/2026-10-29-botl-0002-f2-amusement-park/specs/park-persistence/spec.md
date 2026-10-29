# Spec Delta

## Purpose

Preserves groups, tickets and attractions between executions in text files and provides a temporary execution mode whose data is discarded when execution ends.

## ADDED Requirements

### Requirement: Persist park elements
Groups with their members, individual tickets with their user, group tickets with their composition and (while it exists) their group, and attractions SHALL be stored in text files in the data directory (`groups.txt`, `tickets.txt`, `attractions.txt`) and reconstructed with their relationships at start-up. Java object serialization, databases, frameworks and external libraries SHALL NOT be used.

#### Scenario: State reconstructed after restart
- **WHEN** a group with two members, an individual ticket, a group ticket and an attraction are created and the application is restarted
- **THEN** the group, both tickets (with the group ticket's composition, prices and group) and the attraction exist again

### Requirement: Historical tickets survive deletion of users and groups
Tickets of a deleted user and group tickets of a deleted group SHALL keep the information needed to reconstruct them (identifier, date, price, composition, user identity) across restarts; the group reference of such group tickets SHALL remain empty.

#### Scenario: Ticket of deleted user after restart
- **WHEN** a user with only historical tickets is deleted and the application is restarted
- **THEN** those tickets still exist with their identifier, date, price and the deleted user's DNI and email, and the user is not an active user

#### Scenario: Group ticket of deleted group after restart
- **WHEN** a group whose tickets are all historical is deleted and the application is restarted
- **THEN** its group tickets still exist with their composition and no group

### Requirement: Attractions start empty
Attraction occupants SHALL NOT be persisted; every execution SHALL start with all attractions empty.

#### Scenario: Occupants not restored
- **WHEN** a user is inside an attraction and the application is restarted
- **THEN** the attraction has no occupants

### Requirement: Temporary execution mode
When the first argument is `--fly`, optionally followed by a command file path, the application SHALL create a new directory `tmp/<yyyyMMdd_HHmmss>/`, configure all repositories (users, groups, tickets, attractions) to use only files there, and delete that directory and its files when execution ends via EXIT, the end of the command file or the end of input. The same repository implementations SHALL be used, differing only in paths. Without `--fly`, argument handling SHALL be unchanged.

#### Scenario: Fly with command file leaves no data
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly input_fase2.txt` creates users, groups, tickets and attractions
- **THEN** the commands run normally, the persistent files of normal mode are untouched, and no temporary directory remains after execution

#### Scenario: Fly interactive
- **WHEN** the JAR is started with `--fly` only and receives commands on standard input followed by `EXIT`
- **THEN** commands run normally and the temporary directory is deleted on exit

#### Scenario: Normal mode unchanged
- **WHEN** the JAR is started without arguments or with only a command file path
- **THEN** behaviour is as in Delivery 1, with data in the working directory
