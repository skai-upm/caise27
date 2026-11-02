# delivery-two-integration Specification

## Purpose

Captures the Delivery 2 technical restrictions and the expectation that Delivery 1 and Delivery 2 commands coexist in every execution mode.

## Requirements

### Requirement: Delivery 2 technical restrictions
Delivery 2 SHALL keep the Delivery 1 restrictions (Java 21, Maven, executable JAR, no databases, frameworks or external libraries) and SHALL represent loaded domain relationships with object references and collections in the specified directions: attractions know their users, individual tickets know their user, group tickets compose their individual tickets and reference their group while it exists, and users and groups reference no tickets. (F2-C10)

#### Scenario: Relationship directions
- **WHEN** the domain classes are inspected
- **THEN** relationships use object references in exactly those directions and no user or group holds ticket or attraction references

### Requirement: Delivery 1 and 2 coexistence
All Delivery 1 and Delivery 2 commands SHALL work together in interactive mode, file mode and `--fly` mode, and a mixed command file SHALL keep user, group, ticket and attraction invariants. (F2-C10)

#### Scenario: Fresh build all modes
- **WHEN** `mvn package` runs on a fresh checkout
- **THEN** the JAR runs interactively, with `input_fase2.txt`, and with `--fly input_fase2.txt`

#### Scenario: Mixed Phase 1/2 file
- **WHEN** a command file mixes user commands with system, group, ticket and attraction commands, including invalid ones
- **THEN** only valid commands change state, invariants hold, and the application never terminates unexpectedly
