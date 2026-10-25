# attractions Specification

## Purpose

Models park attractions and controls ticket-based entry and exit, capacity and status queries.

## Requirements

### Requirement: Create and delete attractions
`ATTRACTION CREATE;id;name;capacity` SHALL create an attraction with a unique identifier, a non-empty name and a positive integer maximum capacity. `ATTRACTION DELETE;id` SHALL delete an attraction that has nobody inside.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** attraction A01 named Tornado with capacity 20 exists and is empty

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A02;Noria;0` or `ATTRACTION CREATE;A02;Noria;x` is processed
- **THEN** an `ERROR` is reported

#### Scenario: Delete occupied attraction
- **WHEN** A01 has users inside and `ATTRACTION DELETE;A01` is processed
- **THEN** an `ERROR` is reported and A01 remains

### Requirement: Enter with a ticket
`ATTRACTION ENTER;attraction;ticket` SHALL add the ticket's users to the attraction: the holder of an individual ticket, or the holders of all internal tickets of a group ticket. Both ticket kinds SHALL be handled through the same entry logic.

#### Scenario: Individual entry
- **WHEN** T001 belongs to Ana, is dated today and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana is present in A01

#### Scenario: Group entry
- **WHEN** TG01 has tickets for Ana, Pedro and Luis, is dated today and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** Ana, Pedro and Luis are present in A01

### Requirement: Only tickets for the system date
Entry SHALL be rejected unless the ticket date equals the system date.

#### Scenario: Ticket for another day
- **WHEN** the system date is 2026-10-14 and T001 is dated 2026-10-15
- **THEN** `ATTRACTION ENTER;A01;T001` reports an `ERROR` and nobody enters

### Requirement: Capacity and atomic entry
The number of users present SHALL never exceed the maximum capacity. If not all users of a ticket fit, or any of them is already inside the attraction, none SHALL enter.

#### Scenario: Group does not fit
- **WHEN** A01 has 3 free places and `ATTRACTION ENTER;A01;TG01` is processed for a group ticket with 4 users
- **THEN** an `ERROR` is reported and none of the 4 users enters

#### Scenario: Already inside
- **WHEN** Ana is in A01 and `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** an `ERROR` is reported and occupancy is unchanged

### Requirement: Exit an attraction
`ATTRACTION EXIT;attraction;ticket` SHALL remove the ticket's users that are present and free their places; the ticket date SHALL NOT be checked. If none of the ticket's users is present an `ERROR` SHALL be reported.

#### Scenario: Exit frees capacity
- **WHEN** Ana entered A01 with T001 and `ATTRACTION EXIT;A01;T001` is processed
- **THEN** Ana is no longer present and one more place is available

### Requirement: Attraction status
`ATTRACTION STATUS;id` SHALL display the identifier, name, maximum capacity, number of users present, number of available places and the users present, reflecting the current state without distinguishing individual and group entries.

#### Scenario: Status after entries and exit
- **WHEN** T001 (Ana) and TG01 (Pedro, Luis) entered A01 with capacity 20, then `ATTRACTION EXIT;A01;T001` is processed and `ATTRACTION STATUS;A01` is processed
- **THEN** the status shows A01, Tornado, capacity 20, 2 present, 18 available, and Pedro and Luis as present

### Requirement: Attractions persist empty
Attractions (identifier, name, capacity) SHALL be stored in a `;`-separated text file and restored when the application starts; they SHALL always start empty.

#### Scenario: Restart
- **WHEN** A01 had users inside and the application restarts
- **THEN** A01 exists with capacity 20 and nobody inside
