# attraction-access Specification

## Purpose

Defines how users enter attractions using tickets: ticket validity for the current date, capacity limits and a single access mechanism shared by all ticket types.

## Requirements

### Requirement: Entry with an individual ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an individual ticket SHALL put the ticket's user inside the attraction when the attraction and ticket exist, the ticket is dated on the current system date, the user is not already inside that attraction and a place is free.

#### Scenario: Successful entry
- **WHEN** the system date is 2027-04-15, Ana has ticket `T001` for 2027-04-15 and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** the response is `ENTERED A01: ana@correo.com` and Ana is inside `A01`

#### Scenario: Ticket for another date
- **WHEN** the system date is 2027-04-14 or 2027-04-16 and `ATTRACTION ENTER;A01;T001` is processed for a ticket dated 2027-04-15
- **THEN** an error is reported and nobody enters

#### Scenario: Unknown ticket or attraction
- **WHEN** the ticket or the attraction does not exist
- **THEN** an error is reported and nobody enters

#### Scenario: Already inside
- **WHEN** Ana is inside `A01` and `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** an error is reported and Ana appears once in `A01`

### Requirement: Capacity respected
Nobody SHALL enter an attraction whose occupants already fill its maximum capacity.

#### Scenario: Attraction full
- **WHEN** `A03` has capacity 1, Pedro is inside, and Ana tries to enter with a valid ticket
- **THEN** an error about capacity is reported and Ana does not enter

### Requirement: Entry date checked only once
The ticket date SHALL be checked only at entry; a user's presence inside SHALL NOT depend on later changes of the system date.

#### Scenario: Date changes while inside
- **WHEN** Ana entered `A01` on 2027-04-15 and `SYSTEM DATE;2027-04-16` is processed
- **THEN** Ana is still inside `A01`

### Requirement: Single access mechanism
Access SHALL rely on the common ticket abstraction, each ticket type providing the users it admits, rather than separate access implementations per type.

#### Scenario: Ticket provides its users
- **WHEN** an individual ticket is asked for the users it admits
- **THEN** it returns exactly its user

### Requirement: Deleted users leave attractions
When USER DELETE succeeds, the user SHALL also be removed from any attraction it is inside.

#### Scenario: User deleted while inside
- **WHEN** Ana is inside `A01`, her ticket has become historical and `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** `A01` no longer lists Ana
