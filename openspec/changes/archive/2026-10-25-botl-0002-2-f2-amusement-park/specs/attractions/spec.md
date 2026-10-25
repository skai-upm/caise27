# Spec Delta

## Purpose

Manages park attractions and lets visitors enter and leave them with individual or group tickets, respecting capacity and date validity, and reports their current state.

## ADDED Requirements

### Requirement: Create and delete attractions
`ATTRACTION CREATE;Id;Nombre;Capacidad` SHALL create an attraction with a unique identifier, a name and a positive integer maximum capacity. `ATTRACTION DELETE;Id` SHALL delete an existing attraction that has nobody inside. Attraction definitions SHALL persist between executions, and every attraction SHALL be empty when the application starts.

#### Scenario: Create
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is executed
- **THEN** A01 exists with capacity 20 and no users present

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A02;Noria;0` or `ATTRACTION CREATE;A02;Noria;x` is executed
- **THEN** an error is reported

#### Scenario: Restart empties attractions
- **WHEN** users are inside A01 and the application restarts
- **THEN** A01 exists and has no users present

### Requirement: Enter with a ticket
`ATTRACTION ENTER;Id;Ticket` SHALL add to the attraction the user of an individual ticket, or all users of the individual tickets composing a group ticket, handling both ticket kinds uniformly. The ticket's date SHALL equal the current date.

#### Scenario: Individual entry
- **WHEN** the date is 2026-10-15 and `ATTRACTION ENTER;A01;T001` is executed for T001 dated 2026-10-15
- **THEN** T001's user is present in A01

#### Scenario: Group entry
- **WHEN** `ATTRACTION ENTER;A01;TG01` is executed for a valid group ticket of 3 members
- **THEN** the 3 users are present in A01

#### Scenario: Wrong date
- **WHEN** the ticket's date differs from the current date
- **THEN** entry is rejected

### Requirement: Capacity and atomic entry
Entry SHALL never make the number of users present exceed the maximum capacity. If not all users of a ticket can enter (capacity or any of them already present), none SHALL enter.

#### Scenario: Group exceeds remaining capacity
- **WHEN** 3 places remain and a group ticket with 4 users is used
- **THEN** the operation is rejected and nobody enters

### Requirement: Exit with a ticket
`ATTRACTION EXIT;Id;Ticket` SHALL remove from the attraction the users of that ticket who are present, freeing their places; it SHALL be rejected if none of them is present. The date SHALL NOT be checked on exit.

#### Scenario: Exit frees capacity
- **WHEN** T001's user is in A01 and `ATTRACTION EXIT;A01;T001` is executed
- **THEN** the user is no longer present and available places increase by one

### Requirement: Attraction status
`ATTRACTION STATUS;Id` SHALL display the identifier, name, maximum capacity, number of users present, available places and the users currently present, reflecting the state at that moment regardless of whether users entered individually or with a group ticket.

#### Scenario: Status after entries and exit
- **WHEN** T001 and TG01 (2 users) entered A01 (capacity 20) and then T001 exited
- **THEN** status shows 2 present, 18 available and lists the two group users
