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

### Requirement: Entry with a group ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with a group ticket valid for the current system date SHALL put every user of its composition inside the attraction together; the entry is indivisible: if there is not enough capacity for all of them, or any of them is already inside, nobody enters.

#### Scenario: Whole group enters
- **WHEN** the system date is 2027-04-15, `TG01` (Ana, Pedro) is dated 2027-04-15, `A01` has 20 free places and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** the response lists both emails and Ana and Pedro are inside `A01`

#### Scenario: Not enough room for everyone
- **WHEN** `A02` has only one free place and `ATTRACTION ENTER;A02;TG01` is processed for a two-member group ticket
- **THEN** an error about capacity is reported and neither member enters

#### Scenario: Group ticket for another date
- **WHEN** the group ticket is not dated on the current system date
- **THEN** an error is reported and nobody enters

### Requirement: Leaving an attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove that user from the attraction's occupants and release its place; users who entered with a group ticket SHALL each leave with their own email.

#### Scenario: Member of a group leaves alone
- **WHEN** Ana and Pedro entered `A01` with `TG01` and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** the response is `EXITED A01: ana@correo.com`, Ana is no longer inside, Pedro still is, and one more place is available

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT;A01;luis@correo.com` is processed and Luis is not inside `A01`
- **THEN** an error is reported

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION EXIT;A99;ana@correo.com` is processed
- **THEN** an error is reported
