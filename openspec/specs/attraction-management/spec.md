# attraction-management Specification

## Purpose

Manages the park's attractions and lets ticket holders enter and leave them within capacity, with a status query showing current occupancy.

## Requirements

### Requirement: ATTRACTION CREATE command
`ATTRACTION CREATE;<id>;<name>;<capacity>` SHALL create an attraction with a unique identifier, a name, a positive integer maximum capacity and no users present. Duplicate identifiers, blank values and non-positive or non-numeric capacities SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is executed and no attraction `A01` exists
- **THEN** attraction `A01` named `Tornado` with capacity 20 and no users exists

#### Scenario: Invalid capacity or duplicate
- **WHEN** `ATTRACTION CREATE;A02;Noria;0`, `ATTRACTION CREATE;A02;Noria;diez` or a second `ATTRACTION CREATE;A01;...` is executed
- **THEN** an error is reported and no attraction is created or changed

### Requirement: ATTRACTION ENTER command
`ATTRACTION ENTER;<attractionId>;<ticketId>` SHALL admit the users of the ticket: the ticket's user for an individual ticket, or every constituent's user for a group ticket. Entry SHALL be rejected when the attraction or ticket does not exist, the ticket date differs from the system date, any of those users is already present, or capacity would be exceeded. Entry SHALL be all-or-nothing.

#### Scenario: Individual entry
- **WHEN** ticket `T001` for Ana is dated on the system date and `ATTRACTION ENTER;A01;T001` is executed
- **THEN** Ana is present in `A01`

#### Scenario: Group entry
- **WHEN** group ticket `TG01` for Ana and Pedro is dated on the system date and `ATTRACTION ENTER;A01;TG01` is executed
- **THEN** Ana and Pedro are present in `A01`

#### Scenario: Atomic capacity rejection
- **WHEN** `A01` has three places left and a valid group ticket for four users is used
- **THEN** an error is reported, nobody enters and occupancy is unchanged

#### Scenario: Ticket not for today
- **WHEN** a ticket dated a different day than the system date is used
- **THEN** entry is rejected

### Requirement: ATTRACTION EXIT command
`ATTRACTION EXIT;<attractionId>;<ticketId>` SHALL remove from the attraction the present users of that ticket (its user, or the present constituent users of a group ticket), freeing their places. The ticket date SHALL NOT be revalidated. It SHALL be rejected if the attraction or ticket does not exist or none of its users is present.

#### Scenario: Exit frees capacity
- **WHEN** Ana entered `A01` with `T001` and `ATTRACTION EXIT;A01;T001` is executed
- **THEN** Ana is no longer present and one more place is available

#### Scenario: Exit after date change
- **WHEN** Ana entered with a ticket valid that day and the system date is then changed
- **THEN** `ATTRACTION EXIT` with that ticket still succeeds

### Requirement: ATTRACTION STATUS command
`ATTRACTION STATUS;<attractionId>` SHALL display the identifier, name, maximum capacity, number of users present, number of available places and the users present, regardless of whether they entered individually or with a group ticket.

#### Scenario: Status after entries and exit
- **WHEN** `T001` (Ana) and `TG01` (Pedro, Luis) entered `A01` with capacity 20, then `T001` exits, and `ATTRACTION STATUS;A01` is executed
- **THEN** the output shows `A01`, `Tornado`, capacity 20, 2 present, 18 available, and Pedro and Luis as present users

### Requirement: Attraction persistence
Attraction definitions (identifier, name, capacity) SHALL be persisted and restored at startup. Users present SHALL NOT be persisted: every attraction SHALL be empty when the application starts.

#### Scenario: Restart empties attractions
- **WHEN** Ana is present in `A01` and the application restarts
- **THEN** `A01` exists with its name and capacity and no users present
