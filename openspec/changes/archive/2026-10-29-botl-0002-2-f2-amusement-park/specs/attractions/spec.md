# Spec Delta

## Purpose

Defines park attractions with capacity, and how users enter with individual or group tickets, leave, and are listed and reported.

## ADDED Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with a unique identifier, a name, a maximum capacity (positive integer) and an empty occupant collection. Duplicate or empty identifiers and invalid capacities MUST be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** A01 named `Dragon Rojo` with capacity 20 and no occupants exists

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;diez` is processed
- **THEN** an error is shown and no attraction is created

### Requirement: Entering with a ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL admit the users of an existing ticket whose date equals the current system date: the ticket's user for an individual ticket, or every user in the composition for a group ticket. The same operation MUST handle both ticket types through the common ticket abstraction.

#### Scenario: Individual entry
- **WHEN** the date is 2027-04-15 and `ATTRACTION ENTER;A01;T001` is processed for Ana's ticket dated 2027-04-15
- **THEN** Ana is inside A01

#### Scenario: Group entry
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed for a valid group ticket with Ana and Pedro
- **THEN** Ana and Pedro are inside A01

#### Scenario: Ticket not valid today
- **WHEN** the ticket's date differs from the current system date, or the ticket does not exist
- **THEN** an error is shown and nobody enters

#### Scenario: Presence independent of later date changes
- **WHEN** Ana entered A01 and the system date is changed afterwards
- **THEN** Ana is still inside A01

### Requirement: Atomic capacity check
An entry SHALL succeed only if the attraction has enough free places for all users of the ticket; otherwise none of them enters. A user already inside the attraction MUST NOT be added again.

#### Scenario: Insufficient capacity for group
- **WHEN** A02 has 1 free place and a 2-member group ticket is used
- **THEN** an error is shown and neither member enters

### Requirement: Leaving an attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove that user from the attraction and release the place; users who entered with a group ticket leave individually. A user not inside the attraction MUST be reported as an error.

#### Scenario: Member leaves
- **WHEN** Ana and Pedro entered A01 with TG01 and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** Ana is no longer inside A01 and Pedro still is

### Requirement: Listing occupants
`ATTRACTION LIST;AttractionIdentifier` SHALL display the email addresses of the users currently inside the attraction, or an explicit empty result, without changing its state.

#### Scenario: Empty attraction
- **WHEN** `ATTRACTION LIST;A01` is processed for an empty attraction
- **THEN** a controlled empty result is shown

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL display the identifier, name, maximum capacity, number of users inside, number of available places and the users inside, taken from the attraction's own state, regardless of how users entered.

#### Scenario: Status after entries
- **WHEN** A01 (capacity 20) holds Ana and Pedro
- **THEN** STATUS shows A01, Dragon Rojo, capacity 20, 2 inside, 18 available, and both emails

### Requirement: Unidirectional occupancy
The attraction SHALL know the users inside it; users MUST NOT reference attractions.

#### Scenario: User model unchanged
- **WHEN** the user model is inspected
- **THEN** it has no attraction or ticket reference
