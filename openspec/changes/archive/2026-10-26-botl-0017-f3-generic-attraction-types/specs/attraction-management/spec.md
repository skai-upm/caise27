# Spec Delta

## MODIFIED Requirements

### Requirement: Create attraction
The command `ATTRACTION CREATE;<id>;<name>;<capacity>` SHALL create a `GENERAL` attraction, and `ATTRACTION CREATE;<id>;<name>;<capacity>;<type>` SHALL create an attraction of the given type (`GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`, case-insensitive). The attraction has a unique identifier, a name, a maximum capacity that is a positive integer, its type, and an empty collection of users present; success prints `OK ATTRACTION CREATED`. A duplicate identifier, a blank value, an invalid capacity or an unknown type SHALL be rejected with an `ERROR` line.

#### Scenario: Attraction created
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** attraction `A01` named Tornado with capacity 20, type `GENERAL` and no users present exists

#### Scenario: Explicit type
- **WHEN** `ATTRACTION CREATE;A02;Familiar;30;GROUP` is processed
- **THEN** attraction `A02` of type `GROUP` exists

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A02;Noria;0` or `ATTRACTION CREATE;A02;Noria;veinte` is processed
- **THEN** no attraction is created and an `ERROR` line is printed

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A03;Noria;10;VIP` is processed
- **THEN** no attraction is created and an `ERROR` line lists the valid types

#### Scenario: Duplicate identifier
- **WHEN** `A01` exists and `ATTRACTION CREATE;A01;Otra;5` is processed
- **THEN** an `ERROR` line is printed

### Requirement: Attraction status
The command `ATTRACTION STATUS;<attractionId>` SHALL print, starting with an `OK` line, the attraction identifier, name, type, maximum capacity, number of users currently present, number of available places, and the users present (name, surnames and email), reflecting the state at that moment. Users who entered individually and as part of a group SHALL be shown alike.

#### Scenario: Status after entries
- **WHEN** `ATTRACTION ENTER;A01;T001` and `ATTRACTION ENTER;A01;TG01` succeeded (one plus two users) in `A01` with capacity 20
- **THEN** `ATTRACTION STATUS;A01` shows type `GENERAL`, occupancy 3, 17 available places and the three users

#### Scenario: Status after exit
- **WHEN** `ATTRACTION EXIT;A01;T001` then `ATTRACTION STATUS;A01` are processed
- **THEN** the status shows occupancy 2 and 18 available places

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION STATUS;A99` is processed and `A99` does not exist
- **THEN** an `ERROR` line is printed

### Requirement: Attraction persistence
Attractions SHALL be stored with identifier, name, capacity and type, and restored when the application starts with the same type. Attractions stored without a type (earlier data) SHALL be restored as `GENERAL`. The users present SHALL NOT be persisted: every execution starts with all attractions empty.

#### Scenario: Restart empties attractions
- **WHEN** users are present in `A01` and the application restarts
- **THEN** `A01` exists with the same name, capacity and type and no users present

#### Scenario: Earlier data
- **WHEN** the attractions file contains `A01;Tornado;20`
- **THEN** `A01` is restored as a `GENERAL` attraction

## ADDED Requirements

### Requirement: Ticket kinds accepted by each type
A `GENERAL` attraction SHALL admit individual and group tickets; an `INDIVIDUAL` attraction SHALL admit only individual tickets; a `GROUP` attraction SHALL admit only group tickets; a `SPECIAL` attraction SHALL admit only individual tickets, on which its Special Pass rules are applied. Entering with a ticket kind the attraction does not admit SHALL be rejected with an `ERROR` line naming the attraction type, and nobody SHALL enter.

#### Scenario: Group attraction rejects an individual ticket
- **WHEN** `A02` is of type `GROUP` and `ATTRACTION ENTER;A02;T001` uses an individual ticket
- **THEN** an `ERROR` line is printed and nobody enters

#### Scenario: Individual attraction rejects a group ticket
- **WHEN** `A03` is of type `INDIVIDUAL` and `ATTRACTION ENTER;A03;TG01` uses a group ticket
- **THEN** an `ERROR` line is printed and nobody enters

#### Scenario: Special attraction uses individual tickets
- **WHEN** `A04` is of type `SPECIAL`
- **THEN** a group ticket is rejected and access is evaluated on an individual ticket

#### Scenario: General attraction keeps Delivery 2 behaviour
- **WHEN** `A01` is `GENERAL`
- **THEN** both `T001` and `TG01` can enter as before

### Requirement: Access capabilities with interfaces and bounded generics
Ticket access capabilities SHALL be represented by at least one interface implemented by the ticket classes, and attractions SHALL be a generic type whose type parameter is bounded by that capability; each attraction type SHALL fix the accepted capability, and the type parameter SHALL be used to check and admit tickets rather than being merely declared.

#### Scenario: Accepted capability drives admission
- **WHEN** an attraction receives a ticket
- **THEN** it admits it only if the ticket implements the capability bound to that attraction type, and processes it as that capability
