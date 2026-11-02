# delivery-three-integration Specification

## Purpose

Captures the Delivery 3 technical restrictions and the requirement that commands from all three deliveries coexist in every execution mode (F3-C10).

## Requirements

### Requirement: Delivery 3 technical restrictions
Delivery 3 SHALL use Java 21 and Maven and produce with `mvn package` an executable JAR that runs without an IDE or extra configuration; the only additional runtime library SHALL be the BCrypt dependency. It SHALL use layered packages, the Builder pattern for invoices, Singleton for unique controllers, injected non-singleton repositories, bounded generics with interfaces for attractions, and SHALL NOT delete consumed passes because of use. Delivery 1 and 2 features SHALL remain except where Delivery 3 modifies them.

#### Scenario: Executable JAR with BCrypt
- **WHEN** `mvn package` is run and `java -jar target/nombre-aplicacion.jar` creates a user
- **THEN** the JAR runs standalone and the stored password is a BCrypt hash

### Requirement: End-to-end Delivery 3 behaviour
Commands from Deliveries 1, 2 and 3 SHALL work together in interactive, file, `--fly` interactive and `--fly` file modes, with `HELP` and `HELP EXTENDED` covering all of them.

#### Scenario: Mixed Phase 1–3 command file
- **WHEN** a file mixing user, system, group, ticket, pass, attraction and invoice commands runs in file mode and in `--fly` file mode
- **THEN** both runs produce the same outputs, apart from nothing being left behind by `--fly`
