# Spec Delta

## Purpose

Defines the park's attractions, how visitors enter and leave them with their tickets under date and capacity rules, and how the current occupancy is queried.

## ADDED Requirements

### Requirement: Create attraction
The command `ATTRACTION CREATE;Id;Nombre;Capacidad` SHALL create an attraction with that identifier, name and maximum capacity, initially with no users present. The identifier and name SHALL be non-empty, the identifier SHALL be unique and the capacity SHALL be a positive integer; otherwise an error SHALL be reported and nothing created.

#### Scenario: Attraction created
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is received
- **THEN** attraction `A01` named `Tornado` with capacity 20 and no users present exists

#### Scenario: Invalid capacity or duplicate identifier
- **WHEN** `ATTRACTION CREATE;A02;Noria;0`, `ATTRACTION CREATE;A02;Noria;muchos` or, with `A01` existing, `ATTRACTION CREATE;A01;Otra;5` is received
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
The command `ATTRACTION ENTER;Id;IdTicket` SHALL add to the attraction the users admitted by the ticket: the owner of an individual ticket (standalone or internal to a group ticket), or the owners of all internal tickets of a group ticket. Entry SHALL be rejected with an error, and nobody SHALL enter, if the attraction or ticket does not exist, the ticket date is not exactly the system date, the ticket admits no registered user, any admitted user is already present in that attraction, or the resulting number of users present would exceed the maximum capacity.

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

### Requirement: Exit with a ticket
The command `ATTRACTION EXIT;Id;IdTicket` SHALL remove from the attraction every user admitted by the ticket who is currently present, freeing their places, regardless of the system date. If the attraction or ticket does not exist, or none of its users is present, an error SHALL be reported.

#### Scenario: Individual exit
- **WHEN** Ana entered `A01` with `T001` and `ATTRACTION EXIT;A01;T001` is received
- **THEN** Ana is no longer present and one more place is available

#### Scenario: Nobody present
- **WHEN** no user of `T001` is present in `A01` and `ATTRACTION EXIT;A01;T001` is received
- **THEN** an error is printed

### Requirement: Attraction status
The command `ATTRACTION STATUS;Id` SHALL print the attraction identifier, name, maximum capacity, number of users present, number of available places and the users present, reflecting the state at the time of the command and without distinguishing users who entered individually from those who entered with a group ticket. An unknown attraction SHALL be reported with an error.

#### Scenario: Status after entries and exit
- **WHEN** `ATTRACTION ENTER;A01;T001` (Ana) and `ATTRACTION ENTER;A01;TG01` (Pedro, Luis) succeed on capacity 20, then `ATTRACTION STATUS;A01` is received
- **THEN** the output shows `A01`, `Tornado`, capacity 20, 3 present, 17 available and Ana, Pedro and Luis

#### Scenario: Status after exit
- **WHEN** `ATTRACTION EXIT;A01;T001` then `ATTRACTION STATUS;A01` are received
- **THEN** the output shows 2 present, 18 available and only Pedro and Luis

### Requirement: Attraction persistence
Attraction identifiers, names and capacities SHALL be stored in a text file and restored at startup; users present SHALL NOT be persisted, so every attraction starts empty.

#### Scenario: Restart
- **WHEN** `A01` has users present and the application is restarted
- **THEN** `A01` exists with the same name and capacity and no users present
