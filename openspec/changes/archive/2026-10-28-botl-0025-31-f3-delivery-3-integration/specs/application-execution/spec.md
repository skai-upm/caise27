## ADDED Requirements

### Requirement: Delivery 3 end-to-end execution
A command file combining commands of Deliveries 1, 2 and 3 SHALL execute in file order through the packaged application in normal and `--fly` mode, reporting rejected operations and continuing, persisting the Delivery 3 state in normal mode and leaving no generated data after a `--fly` run.

#### Scenario: Full Delivery 3 command file
- **WHEN** a command file with system, group, ticket, group ticket, typed attraction, pass, entry (normal, SPEED and special), exit, status, invoice and help commands (including invalid ones) and `EXIT` is executed
- **THEN** every command produces its expected result or error message in order, and a restart restores the persisted state without attraction occupants

#### Scenario: Same Delivery 3 run in fly mode
- **WHEN** the same user-independent Delivery 3 command file is executed with and without `--fly` on empty data
- **THEN** both outputs are identical and no temporary directory remains after the `--fly` run

### Requirement: Delivery 3 technical restrictions
The application SHALL keep Java 21, Maven and the executable JAR, and SHALL use layered packages, the Builder pattern for invoices, Singleton for unique controllers, non-singleton repositories injected at initialization, BCrypt as a Maven dependency for passwords, bounded generics and access interfaces for attractions, keep consumed passes, and perform group SpeedPass entry atomically.

#### Scenario: Design constraints hold
- **WHEN** the automated restriction checks run
- **THEN** each of these constraints is confirmed on the code
