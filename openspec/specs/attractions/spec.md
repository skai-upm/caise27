# attractions Specification

## Purpose

Models park attractions with a maximum capacity and the users currently inside, and lets users enter with valid tickets and leave.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;<identifier>;<name>;<capacity>` SHALL create an empty attraction with a unique non-empty identifier, a non-empty name, and a positive integer capacity. It SHALL persist the attraction and print `ATTRACTION CREATED`. Duplicates and invalid values SHALL be rejected with an error.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** `ATTRACTION CREATED` is printed and `A01` exists with capacity 20 and no occupants

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;diez` is processed
- **THEN** an error is printed and `A03` is not created

### Requirement: Entering with a ticket
`ATTRACTION ENTER;<attraction>;<ticket>` SHALL let every holder of the ticket enter together: the ticket's user, or every member user of a group ticket. The ticket SHALL exist and be dated on the current system date. Every holder SHALL be an active user not inside any attraction, and capacity SHALL suffice for all. Otherwise nobody enters and an error is printed. On success it SHALL print `ATTRACTION ENTERED` with the number of users.

#### Scenario: Enter with individual ticket
- **WHEN** ticket `T001` for Ana is dated today and `ATTRACTION ENTER;A02;T001` is processed
- **THEN** Ana is inside `A02`

#### Scenario: Enter with group ticket
- **WHEN** group ticket `TG01` for Ana, Pedro, and Laura is dated today and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** all three users are inside `A01`

#### Scenario: Insufficient capacity is all-or-nothing
- **WHEN** an attraction with 2 free places is entered with a group ticket for 3 users
- **THEN** an error is printed and none of the 3 users enters

#### Scenario: Ticket not valid today
- **WHEN** `ATTRACTION ENTER;A01;T002` is processed for a ticket dated on another day
- **THEN** an error is printed and nobody enters

### Requirement: Presence independent of later date changes
Once users are inside an attraction they SHALL stay there until they exit, regardless of later `SYSTEM DATE` changes.

#### Scenario: Date change after entry
- **WHEN** Ana enters `A02` and then `SYSTEM DATE` moves to the next day
- **THEN** Ana is still inside `A02`

### Requirement: Leaving an attraction
`ATTRACTION EXIT;<attraction>;<email>` SHALL remove the user with that email from the attraction's occupants, freeing one place, and print `ATTRACTION EXITED`. An unknown attraction or a user not inside it SHALL be rejected with an error. Users who entered with a group ticket SHALL leave individually.

#### Scenario: Member of a group leaves alone
- **WHEN** Ana, Pedro, and Laura entered `A01` with `TG01` and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** Ana is no longer inside and Pedro and Laura still are

### Requirement: Listing occupants
`ATTRACTION LIST;<attraction>` SHALL print the email of every user currently inside the attraction, one per line, or a line stating that it is empty. It SHALL NOT change any state.

#### Scenario: Empty attraction
- **WHEN** `ATTRACTION LIST;A02` is processed and nobody is inside `A02`
- **THEN** an empty-attraction message is printed

### Requirement: Attraction status
`ATTRACTION STATUS;<attraction>` SHALL print the attraction's identifier, name, maximum capacity, number of occupants, number of free places, and occupants' emails. The values SHALL be derived from the attraction's own state, whichever way the users entered.

#### Scenario: Status after group entry and one exit
- **WHEN** 3 users entered `A01` (capacity 20) with a group ticket and one has left
- **THEN** the status shows `A01`, `Dragon Rojo`, capacity 20, 2 occupants, 18 free places, and the two remaining emails
