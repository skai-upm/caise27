# delivery-integration Specification

## Purpose
Defines the end-to-end compatibility of the three deliveries: commands of Deliveries 1, 2 and 3 coexist in one command file, run as an executable JAR with an external command file in normal and temporary (`--fly`) modes, and preserve the Delivery 3 technical restrictions.

## Requirements

### Requirement: Commands of the three deliveries coexist
A single command file mixing Delivery 1, Delivery 2 and Delivery 3 commands SHALL be processed in order with every command behaving as specified, with previous behaviour unchanged except where Delivery 3 explicitly changes it (passwords stored as BCrypt values, typed attractions, passes, invoices, extended help).

#### Scenario: Integrated command file in normal mode
- **WHEN** `java -jar target/amusement-park.jar input_fase3.txt` runs a file that registers and updates users, sets the date and price, creates groups, individual and group tickets, typed attractions, SpeedPasses and Special Passes, enters attractions normally, with `SPEED` and with a Special Pass, refunds a pass, generates user and group invoices, deletes a ticket with its passes, shows `HELP` and `HELP EXTENDED`, and ends with `EXIT`
- **THEN** every command produces its specified `OK` or `ERROR` response and no command after `EXIT` runs
- **AND** after a restart the users (with BCrypt passwords), groups, tickets, typed attractions and passes with their consumed state are restored and the rules keep applying

#### Scenario: Integrated command file in temporary mode
- **WHEN** `java -jar target/amusement-park.jar --fly input_fase3.txt` runs the same file
- **THEN** the responses are the same as in normal mode
- **AND** no data generated during the execution remains afterwards and the normal data files are untouched

### Requirement: Delivery 3 technical restrictions preserved
The project SHALL keep building with Java 21 and Maven, producing an executable JAR with `mvn package` that runs without an IDE or extra configuration, with a layered package organization, invoices built with the Builder pattern, singleton controllers, non-singleton repositories configured and injected at initialization, BCrypt as the only runtime dependency, bounded generic attractions restricted by access interfaces, consumed passes kept rather than deleted, and atomic group entries consuming SpeedPasses.

#### Scenario: Restrictions verified by the test suite
- **WHEN** `mvn package` runs
- **THEN** the build succeeds and the automated tests covering layering, singletons and injection, BCrypt, generic attraction access, pass consumption atomicity and the UML consistency pass
