# Spec Delta

## Purpose

Captures the Delivery 2 technical restrictions and the requirement that Delivery 1 and Delivery 2 features work together in every execution mode (F2-C10).

## ADDED Requirements

### Requirement: Delivery 2 technical restrictions
Delivery 2 SHALL keep all Delivery 1 restrictions (Java 21, Maven, executable JAR via `mvn package`, Java standard library only, no databases, frameworks or external libraries). In memory, domain relationships SHALL be held as object references and collections with the specified directions: attractions know their occupants, individual tickets know their user, group tickets are composed of individual tickets and reference their group while it exists, and users and groups do not reference tickets or attractions. Delivery 1 features SHALL NOT be removed or made incompatible.

#### Scenario: Relationship directions in code
- **WHEN** the domain classes are inspected
- **THEN** user and group classes have no ticket or attraction fields, and ticket and attraction classes hold object references rather than identifier strings for users and groups

### Requirement: End-to-end Delivery 2 behaviour
Delivery 1 and Delivery 2 commands SHALL coexist and be operable in interactive mode, file mode, `--fly` interactive mode and `--fly` file mode, with invalid commands reported in a controlled way that preserves all invariants.

#### Scenario: Mixed Phase 1 and Phase 2 command file
- **WHEN** a command file mixing user, system, group, ticket, group ticket and attraction commands, including invalid ones, is run in file mode and in `--fly` file mode
- **THEN** both runs produce the same outputs, accepted operations take effect, and rejected ones leave state unchanged
