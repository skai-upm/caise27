# Spec Delta

## MODIFIED Requirements

### Requirement: Create attraction
The system SHALL create an attraction with `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo`, where `Tipo` is one of `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`, or with `ATTRACTION CREATE;Id;Nombre;Capacidad`, which creates a `GENERAL` attraction. The identifier SHALL be non-empty and unique, the name non-empty and the capacity a positive integer. A new attraction has no users present.

#### Scenario: New attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** attraction `A01` named `Tornado` with capacity 20, type `GENERAL` and no users present exists

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A02;Familiar;30;GROUP` is processed
- **THEN** attraction `A02` of type `GROUP` exists

#### Scenario: Invalid data
- **WHEN** the identifier already exists, the capacity is not a positive integer or the type is unknown
- **THEN** an error is reported and no attraction is created

### Requirement: Attraction status
`ATTRACTION STATUS;IdAtraccion` SHALL display the identifier, name, type, maximum capacity, number of users present, number of available places and the users present, reflecting the current state regardless of whether users entered individually or as part of a group.

#### Scenario: Status after mixed entries and an exit
- **WHEN** T001 (Ana) and TG01 (Pedro, Luis) entered `A01` of capacity 20, then `ATTRACTION EXIT;A01;T001` is processed and `ATTRACTION STATUS;A01` follows
- **THEN** the status shows `A01`, `Tornado`, `GENERAL`, capacity 20, 2 present, 18 available and lists Pedro and Luis

### Requirement: Attraction persistence and empty start
Attractions SHALL be stored with identifier, name, capacity and type and restored at start-up; users present SHALL NOT be persisted, so every attraction starts empty. Stored attractions without a type SHALL be restored as `GENERAL`.

#### Scenario: Restart
- **WHEN** users are inside `A01` and the application restarts
- **THEN** `A01` exists with the same name, capacity and type and no users present

## ADDED Requirements

### Requirement: Accepted ticket kinds per type
`GENERAL` attractions SHALL admit individual and group tickets; `INDIVIDUAL` attractions SHALL admit only individual tickets; `GROUP` attractions SHALL admit only group tickets; `SPECIAL` attractions SHALL admit only individual tickets as the basis for access. Entry with a ticket kind the type does not admit SHALL be rejected without anyone entering.

#### Scenario: GROUP rejects an individual ticket
- **WHEN** `A02` is `GROUP` and `ATTRACTION ENTER;A02;T001` is processed for an individual ticket
- **THEN** an error is reported and nobody enters

#### Scenario: INDIVIDUAL rejects a group ticket
- **WHEN** `A03` is `INDIVIDUAL` and `ATTRACTION ENTER;A03;TG01` is processed
- **THEN** an error is reported and nobody enters

#### Scenario: SPECIAL uses an individual ticket
- **WHEN** `A04` is `SPECIAL` and a valid individual ticket is used
- **THEN** its user is admitted, while a group ticket is rejected

### Requirement: Typed access design
Ticket access capabilities SHALL be represented by at least one interface, and attractions SHALL use bounded generic types to restrict, at compile time, the ticket classes each attraction type handles.

#### Scenario: Compile-time restriction
- **WHEN** code passes a group ticket to the entry operation of an individual-only attraction
- **THEN** it does not compile, because the entry parameter type is bounded to the individual access capability
