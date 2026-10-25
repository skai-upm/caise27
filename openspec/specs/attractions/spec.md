# attractions Specification

## Purpose

Defines park attractions with a maximum capacity, ticket-based entry and exit for individuals and groups, and status queries showing current occupancy.

## Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;id;name;capacity` SHALL create an attraction with a valid unused identifier, a non-empty name and a positive integer capacity, starting with no users present, and print `OK ATTRACTION CREATED`.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** attraction A01 named Tornado with capacity 20 and no users present exists

#### Scenario: Invalid capacity or duplicate identifier
- **WHEN** the capacity is not a positive integer or the identifier already exists
- **THEN** an `ERROR` result is printed

### Requirement: Delete attraction
`ATTRACTION DELETE;id` SHALL delete an existing attraction that has no users present and print `OK ATTRACTION DELETED`.

#### Scenario: Attraction with users present
- **WHEN** users are present in A01 and `ATTRACTION DELETE;A01` is processed
- **THEN** an `ERROR` result is printed and A01 remains

### Requirement: Enter with a ticket
`ATTRACTION ENTER;id;ticketId` SHALL add to the attraction the users admitted by the ticket: its user for an individual ticket, or the users of its individual tickets for a group ticket. Both ticket kinds SHALL be handled uniformly.

#### Scenario: Individual entry
- **WHEN** the system date equals T001's date and A01 has room
- **THEN** `ATTRACTION ENTER;A01;T001` adds T001's user and prints `OK ATTRACTION ENTERED -> 1`

#### Scenario: Group entry
- **WHEN** the system date equals TG01's date, TG01 has 4 individual tickets and A01 has at least 4 free places
- **THEN** `ATTRACTION ENTER;A01;TG01` adds the 4 users

### Requirement: Ticket valid only on its date
Entry SHALL be accepted only when the ticket's date equals the system date. Exit SHALL NOT check the date.

#### Scenario: Ticket for another day
- **WHEN** T001 is dated 2026-10-15 and the system date is 2026-10-14
- **THEN** `ATTRACTION ENTER;A01;T001` prints an `ERROR` result and nobody enters

### Requirement: Capacity and atomic entry
The number of users present SHALL never exceed the capacity. If not all users of a ticket can enter, none SHALL enter. A user already present in the attraction SHALL NOT enter it again.

#### Scenario: Not enough room for the whole group
- **WHEN** A01 has 3 free places and TG01 admits 4 users
- **THEN** `ATTRACTION ENTER;A01;TG01` prints an `ERROR` result and nobody enters

#### Scenario: Ticket already inside
- **WHEN** T001's user is already present in A01
- **THEN** `ATTRACTION ENTER;A01;T001` prints an `ERROR` result

### Requirement: Exit with a ticket
`ATTRACTION EXIT;id;ticketId` SHALL remove from the attraction the ticket's users that are present, freeing their places, and print `OK ATTRACTION EXITED -> <count>`. If none of them is present, an `ERROR` result SHALL be printed.

#### Scenario: Exit frees capacity
- **WHEN** T001's user is in A01 and `ATTRACTION EXIT;A01;T001` is processed
- **THEN** the user is removed and the available places increase by one

### Requirement: Attraction status
`ATTRACTION STATUS;id` SHALL display the identifier, name, maximum capacity, number of users present, number of available places and the users present, reflecting the current state without distinguishing individual from group entries.

#### Scenario: Status after entries and exit
- **WHEN** T001 (1 user) and TG01 (3 users) entered A01 with capacity 20 and then T001 exited
- **THEN** `ATTRACTION STATUS;A01` shows 3 users present, 17 available places and lists the 3 group users

### Requirement: Attraction persistence
Attractions SHALL be stored with identifier, name and capacity and restored at startup. Users present SHALL NOT be persisted: every execution starts with all attractions empty.

#### Scenario: Restart empties attractions
- **WHEN** users are in A01, the application exits and starts again
- **THEN** A01 exists with the same name and capacity and no users present
