# attractions Specification

## Purpose

Defines park attractions, their capacity-limited occupancy, entry with individual or group tickets, exit, and read-only listing and status queries.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with a unique non-empty identifier, a non-empty name, a positive integer maximum capacity, and an empty collection of occupants. The attraction SHALL reference its occupants, and users SHALL NOT reference attractions.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction A01 exists with capacity 20 and no occupants

#### Scenario: Invalid capacity or duplicate
- **WHEN** `ATTRACTION CREATE;A02;X;0`, `ATTRACTION CREATE;A02;X;abc`, or a second `ATTRACTION CREATE;A01;...` is processed
- **THEN** an error is printed and no attraction is created

### Requirement: Entry with a ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL admit the ticket's users when the attraction and the ticket exist and the ticket's date equals the current system date. An individual ticket admits its user, and a group ticket admits every user of its composition. Entry SHALL be all-or-nothing: if the remaining capacity is insufficient, or any of those users is already inside, nobody enters.

#### Scenario: Individual entry
- **WHEN** T001 is dated today and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** T001's user is inside A01

#### Scenario: Group entry
- **WHEN** TG01 for 3 users is dated today and `ATTRACTION ENTER;A01;TG01` is processed with at least 3 free places
- **THEN** all 3 users are inside A01

#### Scenario: Insufficient capacity
- **WHEN** A02 has 2 free places and `ATTRACTION ENTER;A02;TG01` is processed for a 3-user group ticket
- **THEN** an error is printed and no user enters

#### Scenario: Ticket not valid today
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed and T001's date differs from the system date
- **THEN** an error is printed and nobody enters

#### Scenario: Presence independent of later date changes
- **WHEN** a user entered A01 and the system date is then changed
- **THEN** the user remains inside A01

### Requirement: Leaving an attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the user with that email from the attraction's occupants and free one place, regardless of how they entered. An unknown attraction, or a user who is not inside, SHALL produce an error.

#### Scenario: Group member leaves individually
- **WHEN** three users entered A01 with TG01 and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** Ana is no longer inside and the other two remain

### Requirement: Attraction list
`ATTRACTION LIST;AttractionIdentifier` SHALL display the emails of the users currently inside the attraction, or an explicit empty result, without changing any state.

#### Scenario: Empty attraction
- **WHEN** `ATTRACTION LIST;A01` is processed and A01 has no occupants
- **THEN** a controlled empty result is printed

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL display, from the attraction's own state, the identifier, name, maximum capacity, number of users inside, number of available places, and the users inside.

#### Scenario: Status after entries
- **WHEN** A01 (capacity 20) has 3 occupants and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows A01, its name, capacity 20, 3 inside, 17 available, and the 3 occupants
