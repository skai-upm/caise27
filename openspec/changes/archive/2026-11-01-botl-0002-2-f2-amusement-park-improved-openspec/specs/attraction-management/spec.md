# Spec Delta

## Purpose

Manages park attractions and the users currently inside them, admitting visitors with individual or group tickets within capacity and reporting live status.

## ADDED Requirements

### Requirement: Create attraction command
`ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad` SHALL create an attraction with a unique identifier, a nonempty name, a positive integer maximum capacity and no users present. Invalid values or a duplicate identifier SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is executed
- **THEN** attraction `A01` named `Tornado` with capacity 20 and no users present exists

#### Scenario: Invalid attraction
- **WHEN** `ATTRACTION CREATE` uses an existing identifier or a capacity that is not a positive integer
- **THEN** the command is rejected

### Requirement: Enter attraction command
`ATTRACTION ENTER;IdAtraccion;IdTicket` SHALL admit all users of the given ticket (one user for an individual ticket, every constituent user for a group ticket) only if the ticket is dated on the system date, none of those users is already inside, and the attraction has room for all of them. Otherwise nobody SHALL enter.

#### Scenario: Individual entry
- **GIVEN** attraction `A01` with free places and ticket `T001` dated on the system date
- **WHEN** `ATTRACTION ENTER;A01;T001` is executed
- **THEN** the ticket's user is present in `A01`

#### Scenario: Atomic capacity rejection
- **GIVEN** an attraction with three remaining places and a valid group ticket for four people
- **WHEN** the group ticket is used to enter
- **THEN** nobody enters and occupancy is unchanged

#### Scenario: Ticket not for today
- **WHEN** a ticket dated on a different day than the system date is used to enter
- **THEN** entry is rejected

### Requirement: Exit attraction command
`ATTRACTION EXIT;IdAtraccion;IdTicket` SHALL remove from the attraction the users of the given ticket who are present, freeing their places, without checking the ticket date. If none of the ticket's users is present the command SHALL be rejected.

#### Scenario: Date changes after admission
- **GIVEN** a user entered with a ticket valid on the entry date
- **WHEN** the system date changes and `ATTRACTION EXIT` is executed with that ticket
- **THEN** the user leaves and the place is freed

### Requirement: Attraction status command
`ATTRACTION STATUS;IdAtraccion` SHALL display the identifier, name, maximum capacity, number of users present, number of available places and the users present, reflecting the current state regardless of whether users entered individually or with a group ticket.

#### Scenario: Entry, exit and status
- **GIVEN** attraction `A01` with capacity 20
- **WHEN** `ATTRACTION ENTER;A01;T001`, `ATTRACTION ENTER;A01;TG01` (3 users) and `ATTRACTION STATUS;A01` are executed
- **THEN** the status shows 4 users present, 16 available places and lists all four users; after `ATTRACTION EXIT;A01;T001` it shows 3 present and 17 available

### Requirement: Attraction persistence and runtime occupancy
Attraction definitions SHALL be persisted and restored at startup; the users present SHALL NOT be persisted, so every attraction starts empty.

#### Scenario: Attractions start empty
- **GIVEN** attraction `A01` had users present when the application ended
- **WHEN** the application restarts
- **THEN** `A01` exists with its name and capacity and no users present
