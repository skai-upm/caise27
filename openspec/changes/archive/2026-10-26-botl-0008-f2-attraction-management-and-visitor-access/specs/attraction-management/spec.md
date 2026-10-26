# Spec Delta

## Purpose

Defines the park's attractions: how they are created and persisted, how visitors enter and leave them with individual or group tickets under date and capacity rules, and how their current status is queried.

## ADDED Requirements

### Requirement: Create attraction
The command `ATTRACTION CREATE;<id>;<name>;<capacity>` SHALL create an attraction with a unique identifier, a name, a maximum capacity that is a positive integer, and an empty collection of users present, printing `OK ATTRACTION CREATED`. A duplicate identifier, a blank value or an invalid capacity SHALL be rejected with an `ERROR` line.

#### Scenario: Attraction created
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** attraction `A01` named Tornado with capacity 20 and no users present exists

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A02;Noria;0` or `ATTRACTION CREATE;A02;Noria;veinte` is processed
- **THEN** no attraction is created and an `ERROR` line is printed

#### Scenario: Duplicate identifier
- **WHEN** `A01` exists and `ATTRACTION CREATE;A01;Otra;5` is processed
- **THEN** an `ERROR` line is printed

### Requirement: Enter attraction
The command `ATTRACTION ENTER;<attractionId>;<ticketId>` SHALL add to the attraction every user covered by the ticket — the user of an individual ticket, or the users of the individual tickets composing a group ticket — using the same entry logic for both ticket kinds, and print `OK ENTERED <attractionId> -> <n> user(s)`. It SHALL be rejected with an `ERROR` line if the attraction or ticket does not exist.

#### Scenario: Individual entry
- **WHEN** ticket `T001` of Ana is dated on the system date and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana is present in `A01`

#### Scenario: Group entry
- **WHEN** group ticket `TG01` covering Pedro and Luis is dated on the system date and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** Pedro and Luis are present in `A01`

### Requirement: Ticket valid only on its date
A ticket SHALL only allow entry when its date equals the current system date; otherwise the entry SHALL be rejected with an `ERROR` line and nobody enters. The date SHALL NOT be checked when leaving.

#### Scenario: Ticket for another day
- **WHEN** the system date is 2026-10-14 and `ATTRACTION ENTER;A01;T001` is processed for a ticket dated 2026-10-15
- **THEN** an `ERROR` line is printed and nobody enters

### Requirement: Capacity limit and atomic entry
The number of users present SHALL never exceed the maximum capacity. If the users of a ticket do not all fit, or any of them is already present in the attraction, the whole entry SHALL be rejected with an `ERROR` line and no user SHALL enter.

#### Scenario: Group does not fit
- **WHEN** three places remain in `A01` and group ticket `TG01` covers four users
- **THEN** `ATTRACTION ENTER;A01;TG01` is rejected and none of the four users enters

#### Scenario: Already present
- **WHEN** Ana is present in `A01` and `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** an `ERROR` line is printed and occupancy is unchanged

### Requirement: Leave attraction
The command `ATTRACTION EXIT;<attractionId>;<ticketId>` SHALL remove from the attraction the users of that ticket who are present, freeing their places, and print `OK EXITED <attractionId> -> <n> user(s)`. If none of the ticket's users is present, or the attraction or ticket does not exist, it SHALL print an `ERROR` line.

#### Scenario: Exit frees capacity
- **WHEN** Ana entered `A01` with `T001` and `ATTRACTION EXIT;A01;T001` is processed
- **THEN** Ana is no longer present and one more place is available

#### Scenario: Nobody to remove
- **WHEN** none of the users of `T001` is present in `A01`
- **THEN** `ATTRACTION EXIT;A01;T001` prints an `ERROR` line

### Requirement: Attraction status
The command `ATTRACTION STATUS;<attractionId>` SHALL print, starting with an `OK` line, the attraction identifier, name, maximum capacity, number of users currently present, number of available places, and the users present (name, surnames and email), reflecting the state at that moment. Users who entered individually and as part of a group SHALL be shown alike.

#### Scenario: Status after entries
- **WHEN** `ATTRACTION ENTER;A01;T001` and `ATTRACTION ENTER;A01;TG01` succeeded (one plus two users) in `A01` with capacity 20
- **THEN** `ATTRACTION STATUS;A01` shows occupancy 3, 17 available places and the three users

#### Scenario: Status after exit
- **WHEN** `ATTRACTION EXIT;A01;T001` then `ATTRACTION STATUS;A01` are processed
- **THEN** the status shows occupancy 2 and 18 available places

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION STATUS;A99` is processed and `A99` does not exist
- **THEN** an `ERROR` line is printed

### Requirement: Attraction persistence
Attractions SHALL be stored with identifier, name and capacity, and restored when the application starts. The users present SHALL NOT be persisted: every execution starts with all attractions empty.

#### Scenario: Restart empties attractions
- **WHEN** users are present in `A01` and the application restarts
- **THEN** `A01` exists with the same name and capacity and no users present
