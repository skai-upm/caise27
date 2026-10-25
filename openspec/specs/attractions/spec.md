# attractions Specification

## Purpose

Models the park's attractions, letting visitors enter and leave them with individual or group tickets while respecting capacity, and reporting each attraction's current status.

## Requirements

### Requirement: Create and delete attractions
`ATTRACTION CREATE;Id;Name;Capacity` SHALL create an attraction with a unique identifier (letters, digits, `-`, `_`), a non-empty name, a positive integer maximum capacity and no users present. `ATTRACTION DELETE;Id` SHALL delete an attraction that has no users present. Invalid or duplicate values, unknown attractions and deleting an occupied attraction SHALL be rejected.

#### Scenario: Attraction created
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is executed
- **THEN** attraction A01 named Tornado with capacity 20 and no users present exists

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A02;Noria;0` or `ATTRACTION CREATE;A02;Noria;diez` is executed
- **THEN** an error is shown and no attraction is created

### Requirement: Enter attraction
`ATTRACTION ENTER;Id;TicketId` SHALL add to the attraction the users represented by the ticket (the ticket's user for an individual ticket; the users of all its individual tickets for a group ticket), handled uniformly for both ticket types. The ticket's date SHALL equal the system date. Entry SHALL be all-or-nothing: if capacity is insufficient for all of the ticket's users, or any of them is already present, no one enters.

#### Scenario: Individual entry
- **WHEN** T001 is dated on the system date and `ATTRACTION ENTER;A01;T001` is executed
- **THEN** T001's user is present in A01

#### Scenario: Group entry
- **WHEN** TG01 with two members is dated on the system date and `ATTRACTION ENTER;A01;TG01` is executed
- **THEN** both members are present in A01

#### Scenario: Wrong date
- **WHEN** a ticket dated other than the system date is used to enter
- **THEN** an error is shown and no one enters

#### Scenario: Insufficient capacity for group
- **WHEN** three places remain and `ATTRACTION ENTER;A01;TG01` is executed for a group ticket with four users
- **THEN** the entry is rejected and no member enters

### Requirement: Exit attraction
`ATTRACTION EXIT;Id;TicketId` SHALL remove from the attraction the ticket's users that are present, freeing their places, without checking the ticket's date. If none of them is present it SHALL be rejected.

#### Scenario: Exit frees capacity
- **WHEN** T001's user is in A01 and `ATTRACTION EXIT;A01;T001` is executed
- **THEN** the user is no longer present and one more place is available

### Requirement: Attraction status
`ATTRACTION STATUS;Id` SHALL show the identifier, name, maximum capacity, number of users present, number of available places and the users present, reflecting the current state regardless of whether users entered individually or with a group ticket.

#### Scenario: Status after entries and exit
- **WHEN** T001 (one user) and TG01 (two users) entered A01 with capacity 20, then T001 exited, and `ATTRACTION STATUS;A01` is executed
- **THEN** the output shows A01, Tornado, capacity 20, 2 present, 18 available, and the two group members

### Requirement: Attraction persistence
Attractions (identifier, name, capacity) SHALL be stored in a text file next to the users file and restored at startup. Users present SHALL NOT be persisted; every attraction starts empty.

#### Scenario: Attractions restored empty
- **WHEN** A01 had users present and the application restarts
- **THEN** A01 exists with the same name and capacity and no users present
