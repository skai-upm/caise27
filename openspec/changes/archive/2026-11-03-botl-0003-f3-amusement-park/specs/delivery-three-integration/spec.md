# Spec Delta

## Purpose

Captures the complete Delivery-3 state: commands of the three deliveries coexisting under the Delivery-3 technical restrictions in every execution mode.

## ADDED Requirements

### Requirement: Coexistence of all deliveries
Commands of Deliveries 1, 2 and 3 SHALL work together in interactive, file and `--fly` modes, with no earlier feature removed or made incompatible except where Delivery 3 explicitly changes it.

#### Scenario: Mixed Delivery 1 to 3 file
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly input_fase3.txt` runs a file mixing commands of all deliveries
- **THEN** each command behaves as specified and no data remains afterwards

### Requirement: Delivery-3 technical restrictions
The project SHALL use Java 21 and Maven and produce an executable JAR with `mvn package`, use layered packages, the Builder pattern for invoices, Singleton for unique controllers, injected non-singleton repositories, BCrypt via Maven as the only external runtime library, and bounded generics with interfaces for attractions.

#### Scenario: Fresh build
- **WHEN** `mvn package` runs on a fresh checkout
- **THEN** `target/nombre-aplicacion.jar` runs with a command file and with `--fly` without extra configuration
