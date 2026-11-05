# Spec Delta

## ADDED Requirements

### Requirement: Delivery 3 technical restrictions
Delivery 3 SHALL keep all compatible earlier restrictions (Java 21, Maven, an executable JAR from `mvn package`, interactive and command-file execution, `--fly`). It SHALL also have:
- layered packages
- invoices built with the Builder pattern
- Singleton for unique management controllers
- replaceable, injected (non-singleton) repositories
- BCrypt as the only added library
- bounded generic attractions restricted by access interfaces
- consumed passes that are kept, not deleted
- atomic group SpeedPass entry

Delivery 1 and 2 commands SHALL keep working, except where Delivery 3 explicitly changes them.

#### Scenario: All deliveries' commands coexist
- **GIVEN** a phase-3 command file using system, group, ticket, group ticket, attraction (typed, SPEED and special), pass, invoice and help commands
- **WHEN** it runs through the application's command wiring
- **THEN** every command produces its documented result, consumed passes remain stored, and the state is rebuilt after a restart

#### Scenario: Phase-3 file in temporary mode
- **WHEN** `java -jar target/amusement-park.jar --fly input_fase3.txt` runs
- **THEN** the file is processed and no generated data remains afterwards

#### Scenario: Design restrictions hold
- **WHEN** the production design is inspected
- **THEN** central management is a singleton, repositories are not, attraction types bind access interfaces through generics, and invoices are produced by builders extending a common builder abstraction
