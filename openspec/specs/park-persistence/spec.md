# park-persistence Specification

## Purpose
Defines how the park state is stored in text files and reconstructed in later executions, and which state is deliberately not persisted.

## Requirements

### Requirement: Persisted park state
The application SHALL store in text files, and reconstruct on the next execution, the users, the groups with their member users, the individual tickets with their user, the group tickets with their composition and (while the group exists) the purchasing group, the passes with their associated individual ticket, price and consumed state, and the attractions. Java object serialization, databases, persistence frameworks and external libraries SHALL NOT be used.

#### Scenario: State survives a restart
- **WHEN** an execution creates a group with members, an individual ticket, a group ticket and an attraction, ends, and a new execution starts on the same data
- **THEN** the new execution has the same group and members, tickets (identifiers, dates, prices, users, composition, group reference) and attraction (identifier, name, capacity)

#### Scenario: Restored state keeps relationships
- **WHEN** a restarted execution processes `GROUP ADD` on a group locked by an active group ticket created in a previous execution
- **THEN** the operation is rejected because the restored group ticket still references the group

#### Scenario: Passes survive a restart
- **WHEN** an execution buys SpeedPasses and a Special Pass and the application restarts
- **THEN** the passes exist with the same identifiers, prices, associated tickets and consumed state

### Requirement: Attraction occupants not persisted
The collection of users inside each attraction SHALL NOT be persisted; at the start of every execution all attractions SHALL be empty.

#### Scenario: Attractions empty after restart
- **WHEN** users are inside an attraction when an execution ends and a new execution starts
- **THEN** `ATTRACTION LIST` for that attraction shows no users

### Requirement: Historical records survive deletions
Tickets SHALL keep enough data (identifier, date, price, user DNI and e-mail, and for group tickets their composition) to be reconstructed after their user has been deleted or their group has been deleted; a deleted group's tickets SHALL be reconstructed with no group reference.

#### Scenario: Ticket of a deleted user
- **WHEN** a user with only historical tickets is deleted and the application restarts
- **THEN** the historical tickets are reconstructed with the user's DNI and e-mail and the user is not an active user

#### Scenario: Group ticket of a deleted group
- **WHEN** a group with only historical group tickets is deleted and the application restarts
- **THEN** its group tickets are reconstructed with their composition and no group reference

### Requirement: Per-execution system values
The current system date and base price SHALL NOT be persisted: every execution starts with the local date and 40.00 euros.

#### Scenario: Price not carried over
- **WHEN** `SYSTEM PRICE;45.50` is processed and the application restarts
- **THEN** the base price is 40.00

### Requirement: Attraction type persisted
The type of each attraction SHALL be stored and restored; stored attractions without a type SHALL be loaded as GENERAL attractions.

#### Scenario: Typed attraction after restart
- **WHEN** a SPECIAL attraction is created and the application restarts
- **THEN** the attraction is still SPECIAL

#### Scenario: Data without type
- **WHEN** the attraction file contains a line `A01;Dragon Rojo;20` without type
- **THEN** `A01` is loaded as a GENERAL attraction

### Requirement: User attributes persisted
The user file SHALL store one user per line with the attributes separated by `;` in the order `FirstName;Surnames;DNI;Email;Password;Phone;Card`, where Password is the protected (BCrypt) value, and restore them on the next execution; lines written by earlier versions of the application (with fewer fields or the previous attribute order) SHALL still load.

#### Scenario: Full user after restart
- **WHEN** a user with all attributes is saved and the application restarts
- **THEN** the user is restored with the same first name, surnames, DNI, e-mail, protected password, telephone and card

#### Scenario: Earlier user line
- **WHEN** the user file contains `12345678Z;ana@correo.com`
- **THEN** the user loads with that DNI and e-mail and empty remaining attributes

#### Scenario: Assignment line format
- **WHEN** user `Ana Garcia Lopez` (DNI `12345678Z`, e-mail `ana@correo.com`, telephone `612345678`, card `1234567890123456`) is saved
- **THEN** its line is `Ana;Garcia Lopez;12345678Z;ana@correo.com;<bcrypt>;612345678;1234567890123456`, where `<bcrypt>` is the protected password and not `Clave1!`

#### Scenario: Users created by command persist
- **WHEN** `USER CREATE` registers two users and the application is started again
- **THEN** both users are loaded with all their attributes and their passwords verify

### Requirement: Delivery 3 state in both persistence modes
Attraction types and passes (with their individual ticket, stored price and consumed state) SHALL be persisted and reconstructed in normal mode and handled through the same repositories, configured with the temporary directory, in `--fly` mode. Consumed passes SHALL remain stored while their ticket exists, and valid ticket deletions SHALL remove the associated passes from the stored state.

#### Scenario: Consumed pass and typed attraction after restart
- **WHEN** an execution creates a SPECIAL attraction, buys a Special Pass, consumes it by entering, and the application restarts
- **THEN** the attraction is still SPECIAL, the pass still exists with its price and consumed state, and the attraction has no occupants

#### Scenario: Cascading deletion persisted
- **WHEN** a future ticket with passes is deleted and the application restarts
- **THEN** neither the ticket nor its passes are restored

#### Scenario: Fly mode leaves no Delivery 3 data
- **WHEN** the same command file is run with `--fly`
- **THEN** it produces the same output as in normal mode on an empty data directory and no temporary directory remains
