# attractions Specification

## Purpose

Defines park attractions and how users enter, leave and are observed in them using their tickets.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an empty attraction with a unique identifier, a non-blank name and a positive integer capacity (maximum simultaneous occupants). Invalid or duplicate data SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** A01 exists with capacity 20 and no occupants

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;abc` is processed
- **THEN** an error is printed and A03 is not created

### Requirement: Entering with a ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL let the users of an existing ticket dated exactly the current system date enter the attraction: the ticket's user for an individual ticket, all users of its individual tickets for a group ticket. Entry SHALL be all-or-nothing: if capacity is insufficient for everyone, or any of them is already inside that attraction, nobody enters. The date SHALL be checked only at entry.

#### Scenario: Individual entry
- **WHEN** the system date is 2027-04-15 and `ATTRACTION ENTER;A02;T001` is processed for Ana's ticket dated 2027-04-15
- **THEN** Ana is inside A02

#### Scenario: Group entry
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed for a valid group ticket of 3 users and A01 has at least 3 free places
- **THEN** all 3 users are inside A01

#### Scenario: Insufficient capacity
- **WHEN** a group ticket of 3 users is used on an attraction with 2 free places
- **THEN** an error is printed and none of them enters

#### Scenario: Ticket for another day
- **WHEN** a ticket dated 2027-04-20 is used while the system date is 2027-04-15
- **THEN** an error is printed and nobody enters

#### Scenario: Presence survives date change
- **WHEN** Ana is inside A02 and the system date is then changed
- **THEN** Ana is still inside A02

### Requirement: Leaving an attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the given user from the attraction and release one place. A user who is not inside SHALL be rejected. Users who entered with a group ticket SHALL leave individually.

#### Scenario: Leave
- **WHEN** Ana is inside A01 and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** Ana is no longer inside A01 and one more place is available

#### Scenario: Not inside
- **WHEN** `ATTRACTION EXIT;A01;marta@correo.com` is processed and Marta is not inside A01
- **THEN** an error is printed

### Requirement: Listing occupants
`ATTRACTION LIST;AttractionIdentifier` SHALL display the email addresses of the users currently inside the attraction, or a message stating it is empty, without changing its state.

#### Scenario: List occupants
- **WHEN** Ana and Pedro are inside A01 and `ATTRACTION LIST;A01` is processed
- **THEN** the output contains `ana@correo.com` and `pedro@correo.com`

#### Scenario: Empty attraction
- **WHEN** nobody is inside A02 and `ATTRACTION LIST;A02` is processed
- **THEN** a message stating that there are no users is printed

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL display the identifier, name, maximum capacity, number of users inside, number of available places and the users inside, regardless of how they entered.

#### Scenario: Status after group entry
- **WHEN** 3 users entered A01 (capacity 20) with a group ticket and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows A01, Dragon Rojo, capacity 20, 3 inside, 17 available and the three users

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION STATUS;A99` is processed and A99 does not exist
- **THEN** an error is printed
