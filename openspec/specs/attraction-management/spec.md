# attraction-management Specification

## Purpose

Models park attractions with a maximum capacity and lets ticket holders enter and leave them, with atomic group access and an on-demand status report.

## Requirements

### Requirement: Attraction model and creation
An attraction SHALL have a unique identifier, a name, a positive maximum capacity, a type, and the collection of users currently present. `ATTRACTION CREATE;<Id>;<Nombre>;<Capacidad>[;<Tipo>]` SHALL create one (type `GENERAL` when omitted), rejecting duplicate identifiers, empty names, capacities that are not positive integers, and unknown types.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** attraction `A01` named `Tornado` with capacity 20 and no users present exists

#### Scenario: Invalid attraction
- **WHEN** `ATTRACTION CREATE;A02;Noria;0`, `ATTRACTION CREATE;A02;Noria;abc`, or a second `ATTRACTION CREATE;A01;...` is processed
- **THEN** an error message is reported and no attraction is created or changed

### Requirement: Ticket-based entry
`ATTRACTION ENTER;<AtraccionId>;<TicketId>[;SPEED]` SHALL add to the attraction every user admitted by the ticket (the holder of an individual ticket, or the users of all constituent tickets of a group ticket). The ticket identifier MAY name an individual ticket, a group ticket, or a constituent ticket of a group ticket (which then acts as an individual ticket). The ticket kind SHALL be admitted by the attraction type, and the ticket's date SHALL equal the current system date. Entry SHALL be all-or-nothing: if the attraction lacks room for all admitted users, any of them is already present, or a required pass is missing, nobody enters and no pass is consumed.

#### Scenario: Individual entry
- **GIVEN** system date 2026-10-15 and ticket `T001` for Ana dated 2026-10-15
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana is present in `A01`

#### Scenario: Ticket for another date
- **GIVEN** system date 2026-10-14 and ticket `T001` dated 2026-10-15
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** an error message is reported and nobody enters

#### Scenario: Atomic capacity rejection
- **GIVEN** an attraction with three remaining places and a valid group ticket for four people
- **WHEN** the group enters
- **THEN** nobody enters and occupancy remains unchanged

#### Scenario: Capacity never exceeded
- **GIVEN** an attraction with capacity 1 already occupied
- **WHEN** another valid individual ticket is used to enter
- **THEN** an error message is reported and occupancy stays at 1

#### Scenario: Constituent ticket used individually
- **GIVEN** group ticket `TG01` valid today with constituent `TG01-1` for Ana
- **WHEN** `ATTRACTION ENTER;A01;TG01-1` is processed at a `GENERAL` attraction
- **THEN** only Ana enters

### Requirement: Leaving an attraction
`ATTRACTION EXIT;<AtraccionId>;<TicketId>` SHALL remove from the attraction the users admitted by that ticket (individual, group, or constituent ticket) who are present, freeing their places, without revalidating the ticket date. It SHALL be rejected when the attraction or ticket does not exist or none of the ticket's users is present.

#### Scenario: Date changes after admission
- **GIVEN** a user has entered with a ticket valid on the entry date
- **WHEN** the system date changes and `ATTRACTION EXIT` is processed with that ticket
- **THEN** the user leaves the attraction

### Requirement: ATTRACTION STATUS command
`ATTRACTION STATUS;<AtraccionId>` SHALL display the attraction identifier, name, maximum capacity, number of users present, number of available places, and the users present, reflecting the current state regardless of whether users entered individually or with a group ticket.

#### Scenario: Entry, exit, and status
- **GIVEN** attraction `A01` with capacity 20, individual ticket `T001`, and group ticket `TG01` for two users, all valid today
- **WHEN** `ATTRACTION ENTER;A01;T001`, `ATTRACTION ENTER;A01;TG01`, `ATTRACTION STATUS;A01` are processed
- **THEN** the status shows 3 users present, 17 places available, and lists the three users
- **AND WHEN** `ATTRACTION EXIT;A01;T001` and `ATTRACTION STATUS;A01` are processed
- **THEN** the status shows 2 users present and 18 places available

### Requirement: Attraction persistence and daily start
Attraction definitions (identifier, name, capacity, type) SHALL be persisted and restored at startup; stored definitions without a type SHALL load as `GENERAL`. The users present SHALL NOT be persisted: every attraction starts empty at application startup.

#### Scenario: Restart empties attractions
- **GIVEN** attraction `A01` with users present
- **WHEN** the application restarts
- **THEN** `A01` exists with its name and capacity and no users present

#### Scenario: Type survives restart
- **GIVEN** a `SPECIAL` attraction `A02`
- **WHEN** the application restarts
- **THEN** `A02` is still `SPECIAL`
