# delivery-three-integration Specification

## Purpose

Captures the Delivery 3 technical restrictions and the coexistence of the commands of all three deliveries in every execution mode.

## Requirements

### Requirement: Delivery 3 technical restrictions
Delivery 3 SHALL use Java 21 and Maven, produce an executable JAR with `mvn package`, use layered packages, the Builder pattern for invoices, Singleton for unique controllers, non-singleton injected repositories, BCrypt via Maven, and bounded generics with interfaces for attractions; it SHALL NOT use databases, Java serialization or external libraries other than BCrypt. (F3-C10)

#### Scenario: Dependency inspection
- **WHEN** the runtime dependencies are listed
- **THEN** the only one is `org.mindrot:jbcrypt`, bundled in the executable JAR

### Requirement: Delivery 1–3 coexistence
All compatible Delivery 1 and 2 commands and all Delivery 3 commands SHALL work together in interactive, file and `--fly` modes. (F3-C10)

#### Scenario: Mixed command file
- **WHEN** an `input_fase3.txt`-style file mixing commands from all deliveries runs with and without `--fly`
- **THEN** every valid command succeeds, invalid ones are rejected without corrupting state, and `--fly` leaves no data behind
