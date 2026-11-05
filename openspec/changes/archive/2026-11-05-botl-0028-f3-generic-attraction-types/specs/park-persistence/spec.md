## MODIFIED Requirements

### Requirement: Park state persisted between executions
In addition to users, the system SHALL store in text files, and reconstruct at start-up, groups with their users, individual tickets with their user, group tickets with their constituent individual tickets and (while it exists) the purchasing group, and attractions with their identifier, name, capacity and type; a stored attraction without a type (data written by Delivery 2) SHALL be loaded as a `GENERAL` attraction. Changes SHALL be saved after each successful command. Automatic Java object serialization, databases, persistence frameworks and external libraries MUST NOT be used.

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
