# park-persistence Specification

## Purpose

Preserves the complete Amusement Park state — users, groups, tickets and attractions with their relationships — between executions in text files, and provides a temporary persistence mode for isolated runs.

## Requirements

### Requirement: Groups persisted with their users
Groups SHALL be stored in a text file with their identifier, name and member users, and reconstructed at start-up with the same members, after every successful change to a group.

#### Scenario: Group recovered after restart
- **GIVEN** group `G01` named `Familia Garcia` with Ana and Pedro was created in a previous run
- **WHEN** the application starts again
- **THEN** `G01` exists with the same name and members, and `GROUP ADD;G01;ana@correo.com` is rejected as a duplicate member

### Requirement: Tickets persisted with their relationships
Individual tickets SHALL be stored with their identifier, date, price data and user; group tickets with their identifier, date, price data, their constituent individual tickets and, while the group exists, the purchasing group. At start-up they MUST be reconstructed with the same identifiers, dates, prices, users, composition and group link.

#### Scenario: Tickets recovered after restart
- **GIVEN** individual ticket `T001` for Ana priced `45.50` and group ticket `TG01` for `G01` with constituents for Ana and Pedro were created in a previous run
- **WHEN** the application starts again
- **THEN** `T001` belongs to Ana with price `45.50`, `TG01` has the same constituent identifiers and total price and references group `G01`, and the identifiers remain in use

#### Scenario: Group still locked after restart
- **GIVEN** `G01` has an active group ticket persisted in a previous run
- **WHEN** the application starts again and `GROUP ADD;G01;luis@correo.com` is processed on the same date
- **THEN** it is rejected because the group has an active group ticket

### Requirement: Historical records survive deleted users and groups
Ticket records SHALL keep the minimum user data (DNI, first name, surnames, email) and the purchasing group identifier needed to reconstruct historical tickets even when the user or group no longer exists; a group ticket whose group was deleted MUST be reconstructed without a group reference and MUST NOT become linked to a later group that reuses the identifier.

#### Scenario: Ticket of a deleted user
- **GIVEN** Ana had historical ticket `T001` and was then deleted
- **WHEN** the application starts again
- **THEN** `T001` exists with its date and price and Ana's DNI, name and email

#### Scenario: Group ticket of a deleted group
- **GIVEN** group `G01` with historical group ticket `TG01` was deleted, and a new group `G01` was created later
- **WHEN** the application starts again
- **THEN** `TG01` exists with its composition and price, keeps `G01` as purchasing group identifier and has no group reference

### Requirement: Attractions persisted without occupants
Attractions SHALL be stored with their identifier, name, capacity and type and reconstructed at start-up; a stored attraction without a type MUST be loaded as `GENERAL`. The users inside attractions MUST NOT be persisted, so every attraction starts each execution empty.

#### Scenario: Attraction recovered empty
- **GIVEN** attraction `A01` (`Dragon Rojo`, capacity 20) had Ana inside when the previous execution ended
- **WHEN** the application starts again
- **THEN** `A01` exists with the same name and capacity and `ATTRACTION LIST;A01` reports no users inside

#### Scenario: Type recovered and legacy record defaulted
- **GIVEN** the attractions file contains `A04;Pasaje del Terror;15;SPECIAL` and the Delivery 2 line `A01;Dragon Rojo;20`
- **WHEN** the application starts
- **THEN** `A04` is `SPECIAL` and `A01` is `GENERAL`

### Requirement: Text files without serialization or external libraries
All persistent data SHALL be stored in text files written and read by persistence components separate from the model classes and the main class, without automatic Java object serialization, databases, persistence frameworks or external libraries.

#### Scenario: Data files are plain text
- **WHEN** the data files written by an execution are inspected
- **THEN** they are human-readable text lines with `;`-separated fields

### Requirement: Temporary persistence mode
When `--fly` is the first argument, the application SHALL create an independent directory `tmp/<yyyyMMdd_HHmmss>/` named by the start timestamp, configure every repository (users, groups, tickets, attractions) to use only files inside it, and delete that directory and its files when execution ends — after `EXIT`, when a command file finishes normally, or when standard input ends. `--fly` MAY be followed by a command file path; repositories MUST be the same as in normal mode, differing only in configured paths.

#### Scenario: Fly mode with a command file leaves permanent data untouched
- **GIVEN** a permanent `users.txt` with Ana
- **WHEN** `java -jar target/amusement-park.jar --fly input.txt` runs a file that creates Pedro and a group
- **THEN** Ana is not visible during the run, the permanent files are unchanged afterwards, and the temporary directory no longer exists

#### Scenario: Fly mode interactively with EXIT
- **WHEN** `java -jar target/amusement-park.jar --fly` is run and `EXIT` is typed
- **THEN** the temporary directory created at start no longer exists after the application ends

#### Scenario: Normal mode unchanged
- **WHEN** the application is run without `--fly`, with or without a command file
- **THEN** it uses the permanent data files exactly as before
