# attractions Specification

## Purpose

Manages the park's attractions and lets visitors enter and leave them with individual or group tickets under capacity limits, exposing each attraction's current occupancy.

## Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;<id>;<name>;<capacity>` SHALL create an empty `GENERAL` attraction and `ATTRACTION CREATE;<id>;<name>;<capacity>;<type>` an empty attraction of type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`, printing `OK ATTRACTION CREATED`. The identifier and name SHALL be non-empty, the identifier unique, the capacity a positive integer and the type one of the four; otherwise an `ERROR` line SHALL be printed.

#### Scenario: New attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** attraction `A01` named `Tornado` of type `GENERAL` with capacity 20 and no users exists

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A02;Noria;0` or `ATTRACTION CREATE;A02;Noria;muchos` is processed
- **THEN** no attraction is created and an `ERROR` line is printed

#### Scenario: Explicit type
- **WHEN** `ATTRACTION CREATE;A03;Barcas;10;GROUP` is processed
- **THEN** attraction `A03` of type `GROUP` exists

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A04;X;10;VIP` is processed
- **THEN** no attraction is created and an `ERROR` line is printed

### Requirement: Delete attraction
`ATTRACTION DELETE;<id>` SHALL delete an existing attraction with no users present and print `OK ATTRACTION DELETED`; otherwise it SHALL print an `ERROR` line.

#### Scenario: Delete empty attraction
- **WHEN** `ATTRACTION DELETE;A01` is processed and nobody is in `A01`
- **THEN** `A01` no longer exists

### Requirement: Enter with a ticket
`ATTRACTION ENTER;<attractionId>;<ticketId>` SHALL add to the attraction the user of an individual ticket, or the users of all individual tickets composing a group ticket, and print `OK ATTRACTION ENTERED -> <n> users`. Both ticket kinds SHALL be handled through the same entry rules.

#### Scenario: Individual entry
- **WHEN** the system date is 2026-10-15 and `ATTRACTION ENTER;A01;T001` is processed for Ana's ticket dated 2026-10-15
- **THEN** Ana is present in `A01`

#### Scenario: Group entry
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed for a group ticket of two members dated today
- **THEN** both members are present in `A01`

### Requirement: Entry conditions
Entry SHALL be rejected with an `ERROR` line, admitting nobody, if the attraction or ticket does not exist, the ticket's date differs from the system date, any of the ticket's users is already in the attraction, or the free places are fewer than the ticket's users.

#### Scenario: Ticket for another day
- **WHEN** the system date is 2026-10-14 and Ana's ticket is dated 2026-10-15
- **THEN** `ATTRACTION ENTER;A01;T001` is rejected

#### Scenario: Atomic group entry
- **WHEN** three places remain and `ATTRACTION ENTER;A01;TG01` is processed for a group ticket of four users
- **THEN** the entry is rejected and no member enters

### Requirement: Exit with a ticket
`ATTRACTION EXIT;<attractionId>;<ticketId>` SHALL remove the ticket's users present in the attraction, freeing their places, and print `OK ATTRACTION EXITED -> <n> users`, without checking the ticket date. If none of them is present it SHALL print an `ERROR` line.

#### Scenario: Leave and free capacity
- **WHEN** Ana is in `A01` and `ATTRACTION EXIT;A01;T001` is processed
- **THEN** Ana is no longer present and one more place is available

### Requirement: Attraction status
`ATTRACTION STATUS;<id>` SHALL print the attraction identifier, name, type, maximum capacity, number of users present, number of available places and the users present (name, surname and DNI), reflecting entries by individual and group tickets alike.

#### Scenario: Status after entries and exit
- **WHEN** `T001` (Ana) and `TG01` (Pedro, Luis) entered `A01` with capacity 20 and then `ATTRACTION EXIT;A01;T001` is processed
- **THEN** `ATTRACTION STATUS;A01` shows 2 present, 18 available, and lists Pedro and Luis

### Requirement: Attraction persistence
Attractions (identifier, name, capacity and type) SHALL be stored after every change and restored at startup; every attraction SHALL start with no users present. Stored attractions without a type SHALL be restored as `GENERAL`.

#### Scenario: Restart
- **WHEN** `A01` had users present and the application is restarted
- **THEN** `A01` exists with the same name, capacity and type and no users present

### Requirement: Admission by attraction type
`GENERAL` attractions SHALL admit individual and group tickets, `INDIVIDUAL` and `SPECIAL` attractions only individual tickets, and `GROUP` attractions only group tickets. Entering with a ticket kind the attraction does not admit SHALL be rejected with an `ERROR` line and admit nobody.

#### Scenario: GROUP rejects an individual ticket
- **WHEN** `A03` is a `GROUP` attraction and `ATTRACTION ENTER;A03;T001` is processed with an individual ticket
- **THEN** nobody enters and an `ERROR` line is printed

#### Scenario: INDIVIDUAL rejects a group ticket
- **WHEN** `A05` is an `INDIVIDUAL` attraction and `ATTRACTION ENTER;A05;TG01` is processed
- **THEN** nobody enters and an `ERROR` line is printed

#### Scenario: SPECIAL uses an individual ticket
- **WHEN** `A06` is a `SPECIAL` attraction and `ATTRACTION ENTER;A06;T001` is processed with a valid individual ticket
- **THEN** the ticket's user enters, while `ATTRACTION ENTER;A06;TG01` is rejected
