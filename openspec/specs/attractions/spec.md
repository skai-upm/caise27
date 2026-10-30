# attractions Specification

## Purpose

Defines park attractions, how users enter them with valid tickets and leave them, and how their occupancy is consulted.

## Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an empty attraction with a unique identifier, a name and a positive integer capacity. Duplicate identifiers and non-positive or non-numeric capacities SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction A01 exists with capacity 20 and no occupants

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;abc` is processed
- **THEN** an error is printed and A03 is not created

### Requirement: Enter attraction with a ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL let the ticket's users enter when the ticket exists and its date equals the current system date: one user for an individual ticket, every user in the composition for a group ticket. Entry SHALL be all-or-nothing and rejected if capacity is insufficient for everyone or any of them is already inside.

#### Scenario: Individual entry
- **WHEN** T001 for Ana is dated today and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana is inside A01

#### Scenario: Group entry
- **WHEN** TG01 for Ana and Pedro is dated today and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** both Ana and Pedro are inside A01

#### Scenario: Insufficient capacity
- **WHEN** A02 has 1 free place and a two-member group ticket is used
- **THEN** an error is printed and nobody enters

#### Scenario: Ticket not valid today
- **WHEN** the ticket's date differs from the current system date
- **THEN** an error is printed and nobody enters

#### Scenario: Presence independent of later date changes
- **WHEN** Ana entered with a ticket for today and the system date is then changed
- **THEN** Ana is still inside the attraction

### Requirement: Leave attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove that user from the attraction and free one place. A user not inside SHALL be rejected. Users who entered with a group ticket SHALL leave individually.

#### Scenario: Leave
- **WHEN** Ana is inside A01 and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** Ana is no longer inside A01 and one place is freed

### Requirement: List attraction occupants
`ATTRACTION LIST;AttractionIdentifier` SHALL print the emails of the users currently inside, or a controlled empty result, without changing the attraction.

#### Scenario: Empty attraction
- **WHEN** A01 has no occupants and `ATTRACTION LIST;A01` is processed
- **THEN** an empty result message is printed

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL print identifier, name, maximum capacity, number of users inside, available places and the users inside, from the attraction's own state and regardless of how users entered.

#### Scenario: Status after group entry
- **WHEN** A01 (capacity 20) has Ana and Pedro inside
- **THEN** the status shows A01, Dragon Rojo, capacity 20, 2 inside, 18 available, and both emails
