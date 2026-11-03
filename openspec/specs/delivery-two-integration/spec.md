# delivery-two-integration Specification

## Purpose

Captures the Delivery 2 technical restrictions and the requirement that all Phase 1 and Phase 2 commands work together in every execution mode (source F2-C10, sections 15, 16 and 20).

## Requirements

### Requirement: Delivery 2 technical restrictions
Delivery 2 SHALL keep the Delivery 1 restrictions; in memory, domain relationships SHALL be object references and collections (not identifiers or strings) with the specified directions; tickets SHALL share a common abstraction and group tickets SHALL use composition; `--fly` SHALL reuse the normal repositories.

#### Scenario: Relationship directions
- **WHEN** the domain classes are inspected
- **THEN** attractions reference users, individual tickets reference users, group tickets compose individual tickets and reference their group, and neither users nor groups reference tickets or attractions

### Requirement: Phase 1 and Phase 2 commands coexist
All Phase 1 and Phase 2 commands SHALL be operable in the same execution in interactive mode, file mode and `--fly` mode from an executable JAR built with `mvn package`, and Phase 1 behavior SHALL remain compatible except for the ticket and group restrictions on `USER DELETE`.

#### Scenario: Mixed Phase 1 and Phase 2 file
- **WHEN** a command file combining user, system, group, ticket and attraction commands, including invalid ones, is run with `java -jar` normally and with `--fly`
- **THEN** valid commands take effect, invalid ones report errors without terminating the application, and all invariants (uniqueness, locks, capacity, atomicity) hold
