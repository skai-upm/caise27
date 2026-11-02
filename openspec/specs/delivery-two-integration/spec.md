# delivery-two-integration Specification

## Purpose

Captures the Delivery 2 technical restrictions and the integrated behavior of Delivery 1 and Delivery 2 commands working together.

## Requirements

### Requirement: Delivery 2 technical restrictions
Delivery 2 SHALL keep the Delivery 1 restrictions (Java 21, Maven, executable JAR via `mvn package`, interactive and command-file execution, no databases, persistence frameworks or external libraries). In-memory domain relationships SHALL be object references and collections, not identifiers or strings, with these directions: attractions know their occupants; individual tickets know their user; group tickets are composed of individual tickets and reference their group while it exists; users and groups do not reference tickets.

#### Scenario: Restriction inspection
- **WHEN** `pom.xml` and the domain classes are inspected
- **THEN** there are no runtime dependencies and the relationships follow the stated directions

### Requirement: Integrated Delivery 1 and 2 commands
All Delivery 1 and Delivery 2 commands SHALL coexist and work in interactive mode, command-file mode and `--fly` mode, keeping every invariant of users, groups, tickets and attractions.

#### Scenario: Mixed command file in --fly mode
- **WHEN** a command file creating users, groups, individual and group tickets and attractions, entering and leaving attractions and attempting rejected operations is run with `--fly`
- **THEN** every valid operation succeeds, every invalid one is reported without partial changes, and no temporary data remains afterwards
