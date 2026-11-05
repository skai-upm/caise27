# park-persistence Specification

## Purpose

Defines how the park state beyond users — groups, individual and group tickets with their relationships, and attractions — is stored in text files and reconstructed when the application starts again, including historical records.

## Requirements

### Requirement: Park elements persisted in text files
The application SHALL store groups (identifier, name, members), individual tickets (identifier, date, price, user), group tickets (identifier, date, composition, purchasing group while it exists) and attractions (identifier, name, capacity) in text files in the same data directory as the users file, updating them whenever these elements change. Persistence MUST NOT use Java object serialization, databases, frameworks or external libraries, and SHALL be implemented in classes separate from the model and the main class.

#### Scenario: State reconstructed after restart
- **GIVEN** an execution that created users Ana and Pedro, group `G01` with both, ticket `T001` for Ana, group ticket `TG01` for `G01` and attraction `A01` (capacity 20), and then ended
- **WHEN** the application starts again in the same directory
- **THEN** `G01` contains Ana and Pedro, `T001` references Ana with its date and price, `TG01` is composed of `TG01:<dni>` tickets for Ana and Pedro, references `G01` and has the same total price, and `A01` exists with capacity 20

#### Scenario: Reconstructed relationships keep enforcing rules
- **GIVEN** the restarted state above with `TG01` active
- **WHEN** `GROUP ADD;G01;luis@correo.com` or `TICKET CREATE;TG01:12345678Z;ana@correo.com;<date>` is processed
- **THEN** each is rejected with an error message starting with `ERROR`

### Requirement: Attractions start empty
The occupants of attractions SHALL NOT be persisted; at the start of every execution all attractions SHALL be empty.

#### Scenario: Occupants not restored
- **GIVEN** Ana was inside `A01` when the previous execution ended
- **WHEN** the application starts again
- **THEN** `A01` exists and has no occupants

### Requirement: Historical records reconstructable
Historical tickets SHALL be reconstructed even when their user or purchasing group no longer exists: tickets keep the minimum user data needed (DNI, first name, surnames, email) and group tickets of deleted groups are reconstructed with identifier, date, price and composition but no group reference.

#### Scenario: Ticket of a deleted user
- **GIVEN** Ana's ticket `T001` became historical and Ana was deleted
- **WHEN** the application restarts
- **THEN** `T001` is reconstructed with its date, price and Ana's DNI and email, and Ana is not a registered user

#### Scenario: Group ticket of a deleted group
- **GIVEN** group ticket `TG01` became historical and group `G01` was deleted
- **WHEN** the application restarts
- **THEN** `TG01` is reconstructed with its composition and price and without a group reference, and a newly created group `G01` is not linked to it
