# delivery-two-integration Specification

## Purpose

Captures the complete Delivery-2 state: Delivery 1 and 2 commands coexisting under the technical restrictions in every execution mode.

## Requirements

### Requirement: Coexistence of both deliveries
All Delivery 1 and Delivery 2 commands SHALL work together in interactive, file and `--fly` modes, preserving their invariants, with no Delivery-1 feature removed or made incompatible.

#### Scenario: Mixed command file
- **WHEN** a file mixes user, group, ticket and attraction commands, valid and invalid
- **THEN** valid commands take effect, invalid ones report errors, and all invariants hold

### Requirement: Delivery-2 technical restrictions
The project SHALL use Java 21 and Maven, build with `mvn package` into an executable JAR, use no databases, persistence frameworks or external libraries, and keep in-memory relationships as object references in the mandated directions.

#### Scenario: Fresh build and run
- **WHEN** `mvn package` runs on a fresh checkout
- **THEN** the JAR runs interactively, with a command file, and with `--fly`
