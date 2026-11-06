# park-persistence Specification

## Purpose
Defines how the park state (groups, tickets and attractions with their relationships) is stored in text files and rebuilt when the application starts again.

## Requirements

### Requirement: Park state survives restarts
The application SHALL store in text files, inside its data directory, everything needed to rebuild groups and their members, individual tickets and their user, group tickets with their component tickets and (while the group exists) the purchasing group, attractions with their identifier, name, capacity and type (a persisted attraction without a type SHALL be rebuilt as GENERAL), and SpeedPasses and Special Passes with their identifier, stored price (fixed at purchase), associated individual ticket and consumed indicator. Consumed passes SHALL stay persisted while their ticket exists, and passes removed by valid ticket deletions or refunds SHALL disappear from the persisted state. Temporary (`--fly`) mode SHALL persist the same elements through the same repositories. It SHALL rebuild that state at startup. Persistence SHALL NOT use automatic Java object serialization, databases, persistence frameworks or external libraries (the BCrypt dependency is unrelated to persistence).

#### Scenario: Groups, tickets and attractions rebuilt
- **GIVEN** a run that created group `G01` with members, an individual ticket `T001`, a group ticket `TG01` for `G01`, and attraction `A01` with capacity 20, and then ended
- **WHEN** the application starts again with the same data directory
- **THEN** `G01` has the same members, `T001` has the same user, date and price, `TG01` has the same date, total, components and group, and `A01` has the same name and capacity

#### Scenario: Changes persist after each command
- **GIVEN** a run in which `GROUP CREATE;G02;Amigos` was processed
- **WHEN** the run ends without `EXIT`, at the end of its command file, and the application starts again
- **THEN** group `G02` exists

#### Scenario: Passes rebuilt
- **GIVEN** a run that bought SpeedPass `SP01:1` and Special Pass `PE01` for ticket `T001`, with `PE01` consumed
- **WHEN** the application starts again
- **THEN** both passes exist, linked to `T001` with the same prices, and `PE01` is still consumed

#### Scenario: Attraction types rebuilt
- **GIVEN** a SPECIAL attraction `A04`, and an attractions file line `A09;Antigua;5` without a type
- **WHEN** the application starts again
- **THEN** `A04` is SPECIAL and `A09` is GENERAL

#### Scenario: Pass price and state rebuilt from the stored values
- **GIVEN** SpeedPass `SP01:1` priced 8.00 and consumed, and Special Pass `PE01` priced 40.00 and unconsumed, both for ticket `T001`, persisted
- **WHEN** the application restarts
- **THEN** both passes exist for `T001` with the stored prices 8.00 and 40.00, `SP01:1` consumed and `PE01` unconsumed

#### Scenario: Cascaded deletions are persisted
- **GIVEN** future ticket `T002` with SpeedPass `SP02:1`, which is deleted with `TICKET DELETE;T002`
- **WHEN** the application restarts
- **THEN** neither `T002` nor `SP02:1` exists

### Requirement: Occupants are not persisted
The users inside attractions SHALL NOT be persisted. At the start of every run, all attractions SHALL be empty.

#### Scenario: Attractions start empty
- **GIVEN** a run that ended with users inside `A01`
- **WHEN** the application starts again
- **THEN** `A01` exists with no occupants

### Requirement: Historical records survive deleted groups and users
Rebuilding SHALL keep historical tickets as fiscal records. A group ticket whose group was deleted SHALL be rebuilt with no group reference but with its identifier, date, price and composition. A ticket SHALL keep the identity (e-mail) of its user even if that user no longer exists, and a group created later with the same identifier SHALL NOT be linked to it.

#### Scenario: Group deleted before restart
- **GIVEN** historical group ticket `TG01` whose group `G01` was deleted
- **WHEN** the application restarts
- **THEN** `TG01` exists with no group reference and with its original date, price and components

#### Scenario: Same identifier reused after deletion
- **GIVEN** the previous state and a new group created with identifier `G01` before the restart
- **WHEN** the application restarts
- **THEN** `TG01` is still not linked to any group

### Requirement: Controlled handling of unreadable persisted data
If a persisted line can't be interpreted, the application SHALL report a warning, ignore that line and continue starting up with the remaining data. A missing data file SHALL mean that no elements of that type exist yet.

#### Scenario: First run
- **GIVEN** an empty data directory
- **WHEN** the application starts
- **THEN** no error is shown and there are no groups, tickets or attractions

#### Scenario: Corrupted line
- **GIVEN** an attractions file with a malformed line followed by a valid one
- **WHEN** the application starts
- **THEN** a warning is shown and the valid attraction is loaded

### Requirement: Users survive restarts
Registered users SHALL be stored in a text file in the data directory, one user per line, with attributes separated by `;` in the order `FirstName;Surnames;DNI;Email;Password;Phone;Card`. The password field SHALL contain the stored protected (BCrypt) value. Users SHALL be loaded before any other element when the application starts. Reading and writing SHALL be done by a dedicated repository class, not by the user class or the main class. Malformed or duplicate lines SHALL be reported as warnings and ignored.

#### Scenario: Users recovered after restart
- **GIVEN** a run in which `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` was processed
- **WHEN** the application starts again with the same data directory
- **THEN** user `ana@correo.com` exists with the same first name, surnames, DNI, phone and card, and their stored password still verifies `Clave1!`

#### Scenario: Line mapped to fields
- **GIVEN** the users file line `Pedro;Lopez Martin;87654321X;pedro@correo.com;<bcrypt>;623456789;9876543210987654`
- **WHEN** it is loaded
- **THEN** first name `Pedro`, surnames `Lopez Martin`, DNI `87654321X`, e-mail `pedro@correo.com`, phone `623456789` and card `9876543210987654` are assigned to the corresponding attributes

#### Scenario: Plain password never written
- **WHEN** the users file is written after a registration with password `Clave1!`
- **THEN** the file does not contain `Clave1!`

#### Scenario: Duplicate or malformed lines
- **GIVEN** a users file with a line of 3 fields and a second line repeating an existing DNI
- **WHEN** the application starts
- **THEN** a warning is shown for each line and only the valid users are loaded
