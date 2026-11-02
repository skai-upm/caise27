# Spec Delta

## Purpose

Captures the Delivery 3 technical restrictions and the integrated behavior of the commands of all three deliveries.

## ADDED Requirements

### Requirement: Delivery 3 technical restrictions
Delivery 3 SHALL keep the compatible restrictions of Deliveries 1 and 2: Java 21, Maven, an executable JAR via `mvn package` runnable without an IDE or extra configuration, interactive, command-file and `--fly` execution, text-file persistence without serialization or databases, and no external libraries except the BCrypt dependency. Consumed passes SHALL NOT be deleted because of use.

#### Scenario: Dependency inspection
- **WHEN** the runtime dependencies are listed
- **THEN** the only one is the BCrypt library, bundled into `target/nombre-aplicacion.jar`

### Requirement: Integrated commands of all deliveries
All compatible Delivery 1 and 2 commands and all Delivery 3 commands SHALL coexist and work in interactive, file and `--fly` modes.

#### Scenario: Mixed command file in --fly mode
- **WHEN** a command file exercising users, groups, tickets, passes, typed attractions, SpeedPass and Special Pass entries, invoices and rejected operations is run with `--fly`
- **THEN** valid operations succeed, invalid ones are reported without partial changes, and no temporary data remains
