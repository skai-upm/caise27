# Spec Delta

## MODIFIED Requirements

### Requirement: Create attraction
The command `ATTRACTION CREATE;Id;Nombre;Capacidad` SHALL create a GENERAL attraction and `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo` an attraction of type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL` (case-insensitive), with that identifier, name and maximum capacity, initially with no users present. The identifier and name SHALL be non-empty, the identifier SHALL be unique, the capacity SHALL be a positive integer and the type SHALL be one of the four; otherwise an error SHALL be reported and nothing created.

#### Scenario: Attraction created
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is received
- **THEN** attraction `A01` named `Tornado` of type GENERAL with capacity 20 and no users present exists

#### Scenario: Typed attraction created
- **WHEN** `ATTRACTION CREATE;A02;Carrusel;10;GROUP` is received
- **THEN** attraction `A02` of type GROUP exists

#### Scenario: Invalid capacity or duplicate identifier
- **WHEN** `ATTRACTION CREATE;A02;Noria;0`, `ATTRACTION CREATE;A02;Noria;muchos`, `ATTRACTION CREATE;A02;Noria;5;VIP` or, with `A01` existing, `ATTRACTION CREATE;A01;Otra;5` is received
- **THEN** an error is printed and no attraction is created or changed

### Requirement: Enter with a ticket
The command `ATTRACTION ENTER;Id;IdTicket` SHALL add to the attraction the users admitted by the ticket: the owner of an individual ticket (standalone or internal to a group ticket), or the owners of all internal tickets of a group ticket. Entry SHALL be rejected with an error, and nobody SHALL enter, if the attraction or ticket does not exist, the attraction type does not admit that kind of ticket, the ticket date is not exactly the system date, the ticket admits no registered user, any admitted user is already present in that attraction, the resulting number of users present would exceed the maximum capacity, or a type-specific rule fails.

#### Scenario: Individual entry
- **WHEN** the system date is 2026-10-15, ticket `T001` of Ana is dated 2026-10-15 and `ATTRACTION ENTER;A01;T001` is received
- **THEN** Ana is present in `A01`

#### Scenario: Group entry
- **WHEN** group ticket `TG01` dated on the system date covers Pedro and Luis and `ATTRACTION ENTER;A01;TG01` is received
- **THEN** Pedro and Luis are present in `A01`

#### Scenario: Ticket not for today
- **WHEN** the system date is 2026-10-14 or 2026-10-16 and `ATTRACTION ENTER;A01;T001` is received for a ticket dated 2026-10-15
- **THEN** an error is printed and nobody enters

#### Scenario: Group exceeds remaining capacity
- **WHEN** three places remain in `A01` and `ATTRACTION ENTER;A01;TG01` is received for a group ticket covering four users
- **THEN** an error is printed and none of the four users enters

#### Scenario: Capacity exactly reached
- **WHEN** four places remain and a group ticket covering four users is used
- **THEN** all four enter and zero places remain

#### Scenario: Already inside
- **WHEN** Ana is present in `A01` and `ATTRACTION ENTER;A01;T001` is received again
- **THEN** an error is printed and the occupancy is unchanged

#### Scenario: Ticket kind not admitted
- **WHEN** `A02` is GROUP and `ATTRACTION ENTER;A02;T001` (individual) is received, or `A03` is INDIVIDUAL and `ATTRACTION ENTER;A03;TG01` (group) is received
- **THEN** an error indicating the attraction does not admit that ticket kind is printed and nobody enters

### Requirement: Attraction status
The command `ATTRACTION STATUS;Id` SHALL print the attraction identifier, name, type, maximum capacity, number of users present, number of available places and the users present, reflecting the state at the time of the command and without distinguishing users who entered individually from those who entered with a group ticket. An unknown attraction SHALL be reported with an error.

#### Scenario: Status after entries and exit
- **WHEN** `ATTRACTION ENTER;A01;T001` (Ana) and `ATTRACTION ENTER;A01;TG01` (Pedro, Luis) succeed on capacity 20, then `ATTRACTION STATUS;A01` is received
- **THEN** the output shows `A01`, `Tornado`, `GENERAL`, capacity 20, 3 present, 17 available and Ana, Pedro and Luis

#### Scenario: Status after exit
- **WHEN** `ATTRACTION EXIT;A01;T001` then `ATTRACTION STATUS;A01` are received
- **THEN** the output shows 2 present, 18 available and only Pedro and Luis

### Requirement: Attraction persistence
Attraction identifiers, names, capacities and types SHALL be stored in a text file and restored at startup; a stored attraction without type SHALL be restored as GENERAL. Users present SHALL NOT be persisted, so every attraction starts empty.

#### Scenario: Restart
- **WHEN** `A01` has users present and the application is restarted
- **THEN** `A01` exists with the same name, capacity and type and no users present

#### Scenario: Legacy line
- **WHEN** the attraction file contains `A09;Noria;8`
- **THEN** `A09` is restored as a GENERAL attraction

## ADDED Requirements

### Requirement: Attraction types
GENERAL attractions SHALL admit individual and group tickets; INDIVIDUAL attractions only individual tickets (standalone or internal to a group ticket); GROUP attractions only group tickets; SPECIAL attractions only individual tickets that hold an AVAILABLE Special Pass, which SHALL become CONSUMED when the holder enters.

#### Scenario: Special attraction with Special Pass
- **WHEN** `A04` is SPECIAL, ticket `T001` for today has Special Pass `T001-SPECIAL` AVAILABLE and `ATTRACTION ENTER;A04;T001` is received
- **THEN** Ana enters `A04` and `T001-SPECIAL` becomes CONSUMED

#### Scenario: Special attraction without usable Special Pass
- **WHEN** `T002` has no Special Pass, or `T001-SPECIAL` is already CONSUMED, and the ticket is used to enter `A04`
- **THEN** an error is printed and nobody enters

#### Scenario: Group ticket at a special attraction
- **WHEN** `ATTRACTION ENTER;A04;TG01` is received
- **THEN** an error indicating the attraction does not admit that ticket kind is printed
