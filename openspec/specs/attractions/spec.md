# attractions Specification

## Purpose

Lets the park define attractions with a maximum capacity, admit and release visitors through their tickets for the current system date, and report each attraction's live occupancy.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;<Id>;<Nombre>;<Capacidad>` SHALL create an attraction with a unique nonblank identifier, a nonblank name, a maximum capacity given as a positive whole number, and no users present; otherwise it SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** attraction `A01` named `Tornado` with capacity 20 and no users present exists

#### Scenario: Invalid creation
- **WHEN** `ATTRACTION CREATE;A01;Otra;10` (duplicate id), `ATTRACTION CREATE;A02;Loop;0`, `ATTRACTION CREATE;A02;Loop;-3`, `ATTRACTION CREATE;A02;Loop;diez` or `ATTRACTION CREATE;A02;Loop` is processed
- **THEN** it is rejected and no attraction is created or changed

### Requirement: Entry with a ticket
`ATTRACTION ENTER;<IdAtraccion>;<IdTicket>` SHALL admit the users of the ticket: the user of an individual ticket, or the users of every individual ticket composing a group ticket. Entry SHALL be rejected without changes when the attraction or ticket does not exist, the ticket date is not the system date, any of the ticket's users is already present in that attraction, or the users do not all fit in the available places.

#### Scenario: Individual entry
- **GIVEN** system date `2026-10-15`, attraction `A01` with capacity 20 and ticket `T001` for Ana dated `2026-10-15`
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana is present in `A01`

#### Scenario: Group entry
- **GIVEN** group ticket `TG01` dated `2026-10-15` composed of tickets for Pedro and Luis
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** Pedro and Luis are present in `A01`

#### Scenario: Ticket not valid today
- **GIVEN** ticket `T002` dated `2026-10-16` and system date `2026-10-15`
- **WHEN** `ATTRACTION ENTER;A01;T002` is processed
- **THEN** it is rejected and nobody enters

#### Scenario: Insufficient capacity for the whole group
- **GIVEN** `A01` has 3 available places and `TG01` represents 4 users
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** it is rejected and none of the 4 users is present

#### Scenario: Capacity boundary
- **GIVEN** `A01` has exactly as many available places as `TG01` has users
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** all users enter and available places become 0

#### Scenario: Already present
- **GIVEN** Ana is present in `A01`
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** it is rejected and Ana is counted once

### Requirement: Uniform handling of ticket kinds
Entry and exit SHALL obtain the users to admit or release through the common ticket abstraction, so the same attraction logic serves individual and group tickets.

#### Scenario: Same operation for both kinds
- **WHEN** `ATTRACTION ENTER` is processed with an individual ticket and with a group ticket
- **THEN** both are admitted by the same entry operation, which asks the ticket for its users

### Requirement: Exit with a ticket
`ATTRACTION EXIT;<IdAtraccion>;<IdTicket>` SHALL remove from the attraction the ticket's users that are present, freeing their places, without checking the ticket date; it SHALL be rejected when the attraction or ticket does not exist or none of the ticket's users is present.

#### Scenario: Exit frees capacity
- **GIVEN** Ana (via `T001`) and Pedro and Luis (via `TG01`) are present in `A01` with capacity 20
- **WHEN** `ATTRACTION EXIT;A01;T001` and then `ATTRACTION STATUS;A01` are processed
- **THEN** the status shows 2 users present, 18 available places, and Ana is no longer listed

#### Scenario: Exit after the date changed
- **GIVEN** Ana entered with `T001` on `2026-10-15`
- **WHEN** `SYSTEM DATE;2026-10-16` and `ATTRACTION EXIT;A01;T001` are processed
- **THEN** Ana leaves

#### Scenario: Nobody of the ticket present
- **WHEN** `ATTRACTION EXIT;A01;T001` is processed and Ana is not present
- **THEN** it is rejected

### Requirement: Attraction status
`ATTRACTION STATUS;<IdAtraccion>` SHALL display the attraction identifier, name, maximum capacity, number of users present, number of available places and the users present, reflecting the state at that moment and counting each present user once regardless of the ticket kind used; an unknown identifier SHALL be rejected.

#### Scenario: Status after mixed entries
- **GIVEN** `ATTRACTION ENTER;A01;T001` and `ATTRACTION ENTER;A01;TG01` (2 users) succeeded on `A01` with capacity 20
- **WHEN** `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Tornado`, capacity 20, 3 present, 17 available, and the users of `T001` and of `TG01`

### Requirement: Empty attractions at start
Every execution SHALL start with every attraction empty, and a user deleted while present SHALL no longer be counted as present.

#### Scenario: Restart empties attractions
- **GIVEN** users were present in `A01` when the application ended
- **WHEN** the application starts again
- **THEN** `ATTRACTION STATUS;A01` shows 0 users present and full capacity available
