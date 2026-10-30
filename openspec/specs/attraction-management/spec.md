# attraction-management Specification

## Purpose

Models park attractions with a maximum capacity and lets visitors enter with individual or group tickets valid on the system date, leave, and query the current occupancy.

## Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;<id>;<name>;<capacity>` SHALL create an attraction with a unique identifier, non-empty name and positive integer maximum capacity. Invalid or duplicate input SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is executed
- **THEN** attraction `A01` named `Tornado` with capacity 20 and no users present exists

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A02;Rio;0` or `ATTRACTION CREATE;A02;Rio;abc` is executed
- **THEN** an error is reported and no attraction is created

### Requirement: Enter attraction with a ticket
`ATTRACTION ENTER;<id>;<ticketId>` SHALL add to the attraction every user covered by the ticket (its user for an individual ticket, all constituent users for a group ticket). The ticket's date SHALL equal the system date, and the users present plus the entering users SHALL NOT exceed the capacity. Entry SHALL be all-or-nothing; a user already present in that attraction SHALL make the entry fail.

#### Scenario: Individual entry
- **GIVEN** system date 2026-10-15, attraction `A01` with capacity 20 and ticket `T001` dated 2026-10-15
- **WHEN** `ATTRACTION ENTER;A01;T001` is executed
- **THEN** the ticket's user is present in `A01`

#### Scenario: Atomic capacity rejection
- **GIVEN** an attraction with three remaining places and a valid group ticket for four users
- **WHEN** the group ticket is used to enter
- **THEN** an error is reported, nobody enters and occupancy is unchanged

#### Scenario: Ticket for another date
- **WHEN** a ticket dated other than the system date is used to enter
- **THEN** an error is reported and nobody enters

### Requirement: Exit attraction
`ATTRACTION EXIT;<id>;<ticketId>` SHALL remove from the attraction the users of that ticket who are present, freeing their places, without checking the ticket date. If none of them is present, an error SHALL be reported.

#### Scenario: Exit after date change
- **GIVEN** a user entered `A01` with ticket `T001` on its date
- **WHEN** the system date changes and `ATTRACTION EXIT;A01;T001` is executed
- **THEN** the user leaves and one place is freed

### Requirement: Attraction status
`ATTRACTION STATUS;<id>` SHALL display the identifier, name, maximum capacity, number of users present, available places and the users present, regardless of whether they entered individually or with a group ticket.

#### Scenario: Status reflects entries and exits
- **GIVEN** attraction `A01` with capacity 20
- **WHEN** `T001` (1 user) and `TG01` (3 users) enter and then `T001` exits
- **THEN** status shows occupancy 4 with 16 available after the entries and occupancy 3 with 17 available after the exit, listing the present users

### Requirement: Attraction persistence
Attraction definitions (identifier, name, capacity) SHALL be stored in a text file and restored at startup. Users present SHALL NOT be persisted: every attraction starts empty.

#### Scenario: Attractions restart empty
- **GIVEN** attraction `A01` with users present
- **WHEN** the application restarts
- **THEN** `A01` exists with its capacity and no users present
