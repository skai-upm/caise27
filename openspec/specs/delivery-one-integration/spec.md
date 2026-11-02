# delivery-one-integration Specification

## Purpose

Captures the Delivery 1 technical restrictions and the requirement that all first-delivery features work together end to end, in both input modes (F1-C09).

## Requirements

### Requirement: Delivery 1 technical restrictions
Delivery 1 SHALL use Java 21, a standard-structure Maven project built with `mvn package`, an executable JAR, file-based persistence, command interpretation with basic String operations and `split`, and the Java standard library only at runtime. It SHALL NOT use databases, frameworks, external runtime libraries, regular expressions or encryption mechanisms. From Delivery 3 the BCrypt library added as a Maven dependency is permitted at runtime solely to protect stored passwords, superseding the no-external-library and no-encryption restrictions for that purpose only.

#### Scenario: Dependency and restriction inspection
- **WHEN** `pom.xml` and the production sources are inspected
- **THEN** there are no runtime dependencies beyond the JDK, and no use of regular expressions, databases or encryption

#### Scenario: BCrypt is the only runtime dependency from Delivery 3
- **WHEN** `pom.xml` is inspected after Delivery 3
- **THEN** the only non-test dependency is the BCrypt library

### Requirement: End-to-end Delivery 1 behaviour
All Delivery 1 features SHALL work together, and input that cannot be processed correctly SHALL be handled in a controlled manner without corrupting stored users.

#### Scenario: Reference command file
- **WHEN** a fresh build runs `java -jar target/nombre-aplicacion.jar input.txt` with the source's example file (create Ana and Pedro, update Ana's phone and email, delete Pedro, HELP, EXIT)
- **THEN** each command reports success, HELP lists the commands, and afterwards only Ana is stored, with the new phone and email

#### Scenario: Mixed valid and invalid commands
- **WHEN** valid and invalid commands are interleaved in interactive or file mode
- **THEN** accepted operations are persisted, rejected operations are reported, and stored users are not corrupted
