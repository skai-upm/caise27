# atomic-group-entry-and-exit Specification

## Purpose

Extends attraction access to group tickets with indivisible entry, and adds per-user exit and an attraction status view independent of how users entered (source F2-C07, sections 10, 11 and 12).

## Requirements

### Requirement: Indivisible group entry
`ATTRACTION ENTER` with a group ticket valid for the current date SHALL admit all users of its composed individual tickets together, through the same entry operation used for individual tickets, and SHALL admit none when free places are fewer than those users.

#### Scenario: Insufficient capacity for the whole group
- **WHEN** attraction `A02` has 3 free places and a valid group ticket for 4 users is used
- **THEN** an error is reported and none of the 4 users enters

#### Scenario: Whole group enters
- **WHEN** attraction `A01` has 20 free places and valid group ticket `TG01` for 2 users is used
- **THEN** both users are inside `A01`

### Requirement: Exit attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the user with that email from the attraction's occupants, releasing one place; an unknown attraction or a user not inside it SHALL be reported as an error.

#### Scenario: One group member exits
- **WHEN** Ana and Pedro entered `A01` with a group ticket and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** only Ana leaves, Pedro stays and one place is released

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT;A01;luis@correo.com` is processed and Luis is not inside `A01`
- **THEN** an error is reported and occupancy is unchanged

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL display the attraction identifier, name, maximum capacity, number of users inside, number of available places and the users inside, from the attraction's own state, regardless of the ticket type used to enter.

#### Scenario: Status after entries and exit
- **WHEN** two users entered `A01` (capacity 20) with a group ticket and one exited
- **THEN** status shows `A01`, `Dragon Rojo`, capacity 20, 1 inside, 19 available and the remaining user's email
