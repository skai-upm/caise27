# park-persistence Specification

## Purpose
Preserves the park's groups, tickets and attractions between executions in text files, keeps historical tickets reconstructable as fiscal records, and offers a temporary `--fly` mode that uses an isolated, automatically deleted data directory.

## Requirements

### Requirement: Park state persisted between executions
In addition to users, the system SHALL store in text files, and reconstruct at start-up, groups with their users, individual tickets with their user, group tickets with their constituent individual tickets and (while it exists) the purchasing group, attractions with their identifier, name, capacity and type, and SpeedPasses and Special Passes with their identifier, the individual ticket they belong to, their stored price and their consumed or unconsumed state; a stored attraction without a type (data written by Delivery 2) SHALL be loaded as a `GENERAL` attraction. Changes SHALL be saved after each successful command. Consumed passes SHALL remain persisted while their ticket exists, and valid ticket deletions SHALL apply the cascading deletion of passes. The collection of users inside attractions SHALL NOT be persisted. Automatic Java object serialization, databases, persistence frameworks and external libraries (other than the authorized BCrypt dependency) MUST NOT be used.

#### Scenario: Groups and members restored
- **WHEN** group `G01` with members Ana and Pedro exists, the application ends and starts again
- **THEN** group `G01` exists with the same name and with Ana and Pedro as members

#### Scenario: Tickets restored with their relationships
- **WHEN** individual ticket `T001` for Ana and group ticket `TG01` for `G01` with its composition exist, and the application restarts
- **THEN** `T001` references Ana with the same date and price
- **AND** `TG01` references `G01`, has the same date and total price, and its composition tickets reference their users

#### Scenario: Restored state keeps enforcing rules
- **WHEN** after a restart a group ticket of `G01` is still active
- **THEN** `GROUP ADD` on `G01` is rejected and the ticket identifiers remain taken

#### Scenario: Attractions restored empty
- **WHEN** attraction `A01` (capacity 20) had users inside when the application ended
- **THEN** after a restart `A01` exists with capacity 20 and no users inside

#### Scenario: Malformed stored line ignored
- **WHEN** a groups, tickets or attractions file contains a malformed line
- **THEN** that line is ignored, a warning reports the number of ignored lines, and the rest of the state is loaded

#### Scenario: Attraction types restored
- **WHEN** attractions of types `GENERAL`, `INDIVIDUAL`, `GROUP` and `SPECIAL` exist and the application restarts
- **THEN** each attraction is restored with its type

#### Scenario: Delivery 2 attraction line loaded as general
- **WHEN** the attractions file contains the line `A01;Dragon Rojo;20`
- **THEN** after start-up attraction `A01` exists with type `GENERAL` and capacity 20

#### Scenario: Passes restored with ticket, price and state
- **GIVEN** ticket `T001` (40.00 EUR) with SpeedPasses `SP02:1` (consumed) and `SP02:2` (unconsumed) and Special Pass `PE01` (consumed), and the system price later changed
- **WHEN** the application restarts
- **THEN** `SP02:1` and `SP02:2` belong to `T001`, cost `8.00` EUR, and keep their consumed and unconsumed states
- **AND** `PE01` belongs to `T001`, costs `40.00` EUR and is consumed, so a new Special Pass for `T001` is still rejected

#### Scenario: Refunded and cascaded passes are not restored
- **WHEN** a SpeedPass is refunded and a future ticket with passes is deleted, and the application restarts
- **THEN** neither the refunded pass nor the passes of the deleted ticket exist

#### Scenario: Malformed pass line ignored
- **WHEN** the passes file contains a line whose ticket does not exist, whose price or state is invalid, or a second Special Pass for the same ticket
- **THEN** that line is ignored and reported in the warning, and the other passes are loaded

### Requirement: Historical tickets reconstructable after deletions
Tickets SHALL retain the minimum information needed to reconstruct them as fiscal records even if their user or purchasing group no longer exists: identifier, date, price, holder identity (DNI, email, first name, surnames) and, for group tickets, the composition. A group ticket whose group was deleted SHALL be restored with no group reference.

#### Scenario: Ticket of a deleted user
- **WHEN** Ana's only ticket is historical, Ana is deleted, and the application restarts
- **THEN** the ticket is restored with its identifier, date, price and Ana's DNI and email, while Ana is not an active user

#### Scenario: Group ticket of a deleted group
- **WHEN** group `G01` with only a historical group ticket `TG01` is deleted and the application restarts
- **THEN** `TG01` is restored with its date, price and composition and with no group reference

### Requirement: Temporary persistence mode
When `--fly` is the first argument, the application SHALL create a new directory `tmp/<yyyyMMdd_HHmmss>/` (with a suffix if that name exists), and every repository (users, groups, tickets, attractions and passes) SHALL use only files inside it, with the same repositories and persistence mechanisms as normal mode, selected by path configuration and not by alternative repository implementations. When execution ends — through `EXIT` or because standard input or the command file ends — that directory and all its files SHALL be deleted. `--fly` MAY be followed by a command file path; without `--fly`, the normal persistent files are used as before.

#### Scenario: Fly mode leaves normal data untouched
- **WHEN** `java -jar target/amusement-park.jar --fly input.txt` runs a file that creates users, groups and tickets
- **THEN** the normal `users.txt`, `groups.txt`, `tickets.txt`, `attractions.txt` and `passes.txt` are unchanged
- **AND** after the run no `tmp/<timestamp>/` directory remains

#### Scenario: Fly mode interactive with EXIT
- **WHEN** the application is started with `--fly`, creates a user interactively and receives `EXIT`
- **THEN** the temporary directory is deleted and the normal users file does not contain that user

#### Scenario: Fly runs are isolated from each other
- **WHEN** two consecutive `--fly` runs create the same user
- **THEN** both creations succeed
