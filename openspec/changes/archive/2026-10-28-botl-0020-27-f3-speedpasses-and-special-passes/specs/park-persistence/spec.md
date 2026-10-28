## MODIFIED Requirements

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
