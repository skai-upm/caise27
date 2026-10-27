# persistence Specification

## Purpose

Defines how the application's state is preserved between runs using plain text files, and how it is restored when the application starts again.

## Requirements

### Requirement: Users file format
Users SHALL be stored in a text file where each user occupies one line with its attributes separated by `;` in the order first name, surnames, DNI, email, password, telephone, card number.

#### Scenario: Stored line format
- **WHEN** the user Ana with surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678` and card `1234567890123456` is stored
- **THEN** the users file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Users preserved between runs
Users created in one execution SHALL be available in later executions: previously stored users SHALL be loaded when the application starts.

#### Scenario: User survives a restart
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed, the application ends, and the application starts again
- **THEN** the user with email `ana@correo.com` exists in the new execution

#### Scenario: Loaded users keep uniqueness
- **WHEN** the users file already contains a user with DNI `12345678Z` and the application processes a `USER CREATE` with DNI `12345678Z`
- **THEN** the creation is rejected as a duplicate DNI

#### Scenario: No users file yet
- **WHEN** the application starts and the users file does not exist
- **THEN** it starts with no users and without error

### Requirement: Persistence isolated from model and entry point
Reading and writing the users file SHALL be done by a component separate from the user representation and the application entry point, using only the Java standard library (no databases, persistence frameworks or external persistence libraries).

#### Scenario: Responsibilities separated
- **WHEN** the source code is inspected
- **THEN** neither the user class nor the main class performs file reading or writing of users

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

### Requirement: Protected passwords in the users file
The users file SHALL contain the BCrypt-protected value of each password and SHALL NOT contain the original password; previously stored plain-text files are not migrated.

#### Scenario: File without plain passwords
- **WHEN** Ana is created with password `Clave1!` and the users file is inspected
- **THEN** the line for Ana contains a BCrypt value in the password position and the text `Clave1!` does not appear in the file

#### Scenario: Verification after restart
- **WHEN** the application restarts and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** the stored BCrypt value is loaded and verifies the password, and the update succeeds

### Requirement: Attraction type persisted
The type of each attraction SHALL be stored and restored; stored attractions without a type SHALL be interpreted as GENERAL.

#### Scenario: Type survives restart
- **WHEN** SPECIAL attraction `A04` is created, the application ends and starts again
- **THEN** `A04` is a SPECIAL attraction

#### Scenario: Delivery 2 data
- **WHEN** the attractions file contains the line `A01;Dragon Rojo;20`
- **THEN** `A01` is loaded as a GENERAL attraction with capacity 20
