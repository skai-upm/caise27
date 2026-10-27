# Spec Delta

## ADDED Requirements

### Requirement: Park state preserved between runs
Groups and their users, individual tickets and their user, group tickets with their composed individual tickets and (while the group exists) the purchasing group, and attractions SHALL be stored in text files and reconstructed with their relationships when the application starts again. Automatic Java object serialization SHALL NOT be used.

#### Scenario: Groups survive a restart
- **WHEN** group `G01` with Ana and Pedro is created, the application ends and starts again
- **THEN** `G01` exists with Ana and Pedro as members

#### Scenario: Tickets survive a restart
- **WHEN** ticket `T001` for Ana at `40.00` and group ticket `TG01` for `G01` (Ana, Pedro) are created, the application ends and starts again
- **THEN** `T001` belongs to Ana with price `40.00`, and `TG01` references `G01` and is composed of `TG01:12345678Z` and `TG01:87654321X` with the same prices

#### Scenario: Attractions survive a restart
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed, the application ends and starts again
- **THEN** attraction `A01` named `Dragon Rojo` with capacity 20 exists

#### Scenario: Locks reconstructed
- **WHEN** after a restart `G01` has an active group ticket
- **THEN** `GROUP ADD` on `G01` is still rejected

### Requirement: Historical records preserved
Historical tickets SHALL be reconstructed after a restart even if their user has been deleted or their group has been deleted, keeping identifier, date, price, composition and the user data needed to identify each ticket's user; a deleted group's tickets SHALL have no group reference.

#### Scenario: Ticket of a deleted user
- **WHEN** Ana's ticket `T001` became historical, Ana was deleted, and the application restarts
- **THEN** `T001` still exists with its date, price and Ana's DNI, and Ana is not an active user

#### Scenario: Ticket of a deleted group
- **WHEN** `G01` was deleted after its group ticket `TG01` became historical, and the application restarts
- **THEN** `TG01` exists with its composition and price and without a group reference

### Requirement: Attractions start empty
The users inside attractions SHALL NOT be persisted; at the start of each execution all attractions SHALL be empty.

#### Scenario: Occupants not restored
- **WHEN** Ana is inside `A01` when the application ends and the application starts again
- **THEN** `A01` exists and has no users inside

### Requirement: Temporary persistence mode
When `--fly` is the first argument, the application SHALL create a new directory identified by a timestamp (`tmp/yyyyMMdd_HHmmss/`), and all repositories SHALL use only files inside it, through the same repository implementations configured with different paths. When the execution ends through `EXIT` or because the input ends, the directory and all its files SHALL be deleted. Normal data files SHALL NOT be modified by a `--fly` execution.

#### Scenario: Fly run leaves no data
- **WHEN** `java -jar target/amusement-park.jar --fly input.txt` creates users, groups, tickets and attractions and ends
- **THEN** no file under the temporary directory remains and the normal data files are unchanged

#### Scenario: Fly run is isolated
- **WHEN** a normal run stored user Ana and a `--fly` run then starts
- **THEN** the `--fly` run starts without Ana and can create Ana again

#### Scenario: Fly interactive
- **WHEN** `java -jar target/amusement-park.jar --fly` is run and `EXIT` is typed
- **THEN** the temporary directory used during the run has been deleted
