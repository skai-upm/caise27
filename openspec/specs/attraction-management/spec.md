# attraction-management Specification

## Purpose

Defines the park's attractions, how visitors enter and leave them with their tickets under date and capacity rules, and how the current occupancy is queried.

## Requirements

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

### Requirement: Delete attraction
The command `ATTRACTION DELETE;Id` SHALL delete an existing attraction that has no users present; otherwise an error SHALL be reported.

#### Scenario: Empty attraction deleted
- **WHEN** attraction `A01` has no users present and `ATTRACTION DELETE;A01` is received
- **THEN** `A01` no longer exists

#### Scenario: Occupied attraction
- **WHEN** attraction `A01` has users present and `ATTRACTION DELETE;A01` is received
- **THEN** an error is printed and `A01` is kept

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

### Requirement: Exit with a ticket
The command `ATTRACTION EXIT;Id;IdTicket` SHALL remove from the attraction every user admitted by the ticket who is currently present, freeing their places, regardless of the system date. If the attraction or ticket does not exist, or none of its users is present, an error SHALL be reported.

#### Scenario: Individual exit
- **WHEN** Ana entered `A01` with `T001` and `ATTRACTION EXIT;A01;T001` is received
- **THEN** Ana is no longer present and one more place is available

#### Scenario: Nobody present
- **WHEN** no user of `T001` is present in `A01` and `ATTRACTION EXIT;A01;T001` is received
- **THEN** an error is printed

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

### Requirement: Attraction types
GENERAL attractions SHALL admit individual and group tickets; INDIVIDUAL attractions only individual tickets (standalone or internal to a group ticket); GROUP attractions only group tickets; SPECIAL attractions only individual tickets whose Special Pass is AVAILABLE. At a SPECIAL attraction the Special Pass SHALL be located automatically and SHALL become CONSUMED only after a successful entry; the `SPEED` option SHALL be rejected at SPECIAL attractions.

#### Scenario: Special attraction with Special Pass
- **WHEN** `A04` is SPECIAL, ticket `T001` for today has Special Pass `T001-SPECIAL` AVAILABLE and `ATTRACTION ENTER;A04;T001` is received
- **THEN** Ana enters `A04`, `T001-SPECIAL` becomes CONSUMED and the response reports that `T001-SPECIAL` was consumed

#### Scenario: Special attraction without usable Special Pass
- **WHEN** `T002` has no Special Pass, or `T001-SPECIAL` is already CONSUMED, and the ticket is used to enter `A04`
- **THEN** an error is printed and nobody enters

#### Scenario: Group ticket at a special attraction
- **WHEN** `ATTRACTION ENTER;A04;TG01` is received
- **THEN** an error indicating the attraction does not admit that ticket kind is printed

#### Scenario: SPEED at a special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is received
- **THEN** an error is printed, nobody enters and no pass is consumed

#### Scenario: Special Pass kept when entry fails
- **WHEN** `A04` is full and `ATTRACTION ENTER;A04;T001` is received
- **THEN** an error is printed and `T001-SPECIAL` stays AVAILABLE

### Requirement: Entry with SpeedPasses
The command `ATTRACTION ENTER;Id;IdTicket;SPEED` SHALL perform the normal entry and additionally consume one AVAILABLE SpeedPass per admitted individual ticket: the ticket itself for an individual ticket (at GENERAL and INDIVIDUAL attractions) or each internal ticket of a group ticket (at GENERAL and GROUP attractions). If any of those tickets has no AVAILABLE SpeedPass, or any other entry condition fails, nobody SHALL enter and no pass SHALL be consumed. Any fourth parameter other than `SPEED` SHALL be rejected with an error.

#### Scenario: Individual SPEED entry
- **WHEN** `T001` (for today) has AVAILABLE SpeedPasses `T001-S1` and `T001-S2` and `ATTRACTION ENTER;A01;T001;SPEED` is received
- **THEN** Ana enters, exactly `T001-S1` becomes CONSUMED and the response reports it

#### Scenario: Group SPEED entry
- **WHEN** group ticket `TG01` has internal tickets `TG01-1` and `TG01-2`, each with an AVAILABLE SpeedPass, and `ATTRACTION ENTER;A01;TG01;SPEED` is received
- **THEN** both members enter and one SpeedPass of each internal ticket becomes CONSUMED

#### Scenario: Group SPEED entry is atomic
- **WHEN** `TG01-2` has no AVAILABLE SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is received
- **THEN** an error is printed, nobody enters and the SpeedPass of `TG01-1` stays AVAILABLE

#### Scenario: Normal entry consumes nothing
- **WHEN** `ATTRACTION ENTER;A01;T001` is received for a ticket with SpeedPasses
- **THEN** Ana enters and all her SpeedPasses stay AVAILABLE

#### Scenario: Unknown option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is received
- **THEN** an error is printed and nobody enters

### Requirement: Pass consumption recorded
Passes consumed by an attraction entry SHALL be reported in the response and their CONSUMED status SHALL be persisted, so it is kept after a restart.

#### Scenario: Consumption survives restart
- **WHEN** `T001-S1` is consumed by a SPEED entry and the application is restarted
- **THEN** `T001-S1` is CONSUMED and cannot be refunded
