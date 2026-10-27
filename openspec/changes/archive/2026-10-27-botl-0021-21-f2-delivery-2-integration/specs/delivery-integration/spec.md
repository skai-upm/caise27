# Spec Delta

## ADDED Requirements

### Requirement: Delivery 2 reference flow
Phase 1 and Phase 2 commands SHALL coexist: a command file mixing user, system, group, ticket, group ticket and attraction commands SHALL run from `java -jar target/nombre-aplicacion.jar input_fase2.txt` and from `java -jar target/nombre-aplicacion.jar --fly input_fase2.txt` with identical output, the latter leaving no data behind.

#### Scenario: Mixed command file in both modes
- **WHEN** the same Phase 2 command file runs once normally on an empty data directory and once with `--fly`
- **THEN** both outputs are identical and only the normal run leaves data files

### Requirement: Delivery 2 technical restrictions
The application SHALL keep the Delivery 1 restrictions and SHALL represent relationships with object references in the specified directions: users hold no references to tickets, groups or attractions; groups hold no references to tickets; the central park controller has a single instance.

#### Scenario: Relationship directions verified
- **WHEN** the user and group classes are inspected
- **THEN** they declare no fields of ticket, group or attraction types (besides a group's members)
