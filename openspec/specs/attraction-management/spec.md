# attraction-management Specification

## Purpose

Models park attractions with a maximum capacity and lets visitors enter with individual or group tickets valid on the system date, leave, and query the current occupancy.

## Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;<id>;<name>;<capacity>` SHALL create a `GENERAL` attraction with a unique identifier, non-empty name and positive integer maximum capacity. `ATTRACTION CREATE;<id>;<name>;<capacity>;<type>` SHALL create an attraction of the given type (`GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`, case-insensitive). Invalid or duplicate input, including an unknown type, SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is executed
- **THEN** attraction `A01` named `Tornado` with capacity 20, type `GENERAL` and no users present exists

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A02;Rio;0` or `ATTRACTION CREATE;A02;Rio;abc` is executed
- **THEN** an error is reported and no attraction is created

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A03;Castillo;10;SPECIAL` is executed
- **THEN** a `SPECIAL` attraction `A03` exists; `ATTRACTION CREATE;A04;X;10;VIP` is rejected

### Requirement: Enter attraction with a ticket
`ATTRACTION ENTER;<id>;<ticketId>` SHALL add to the attraction every user covered by the ticket (its user for an individual ticket, all constituent users for a group ticket). The ticket SHALL be of a kind accepted by the attraction type, its date SHALL equal the system date, and the users present plus the entering users SHALL NOT exceed the capacity. Entry SHALL be all-or-nothing; a user already present in that attraction SHALL make the entry fail.

#### Scenario: Individual entry
- **GIVEN** system date 2026-10-15, attraction `A01` with capacity 20 and ticket `T001` dated 2026-10-15
- **WHEN** `ATTRACTION ENTER;A01;T001` is executed
- **THEN** the ticket's user is present in `A01`

#### Scenario: Atomic capacity rejection
- **GIVEN** an attraction with three remaining places and a valid group ticket for four users
- **WHEN** the group ticket is used to enter
- **THEN** an error is reported, nobody enters and occupancy is unchanged

#### Scenario: Ticket for another date
- **WHEN** a ticket dated other than the system date is used to enter
- **THEN** an error is reported and nobody enters

### Requirement: Exit attraction
`ATTRACTION EXIT;<id>;<ticketId>` SHALL remove from the attraction the users of that ticket who are present, freeing their places, without checking the ticket date. If none of them is present, an error SHALL be reported.

#### Scenario: Exit after date change
- **GIVEN** a user entered `A01` with ticket `T001` on its date
- **WHEN** the system date changes and `ATTRACTION EXIT;A01;T001` is executed
- **THEN** the user leaves and one place is freed

### Requirement: Attraction status
`ATTRACTION STATUS;<id>` SHALL display the identifier, name, type, maximum capacity, number of users present, available places and the users present, regardless of whether they entered individually or with a group ticket.

#### Scenario: Status reflects entries and exits
- **GIVEN** attraction `A01` with capacity 20
- **WHEN** `T001` (1 user) and `TG01` (3 users) enter and then `T001` exits
- **THEN** status shows occupancy 4 with 16 available after the entries and occupancy 3 with 17 available after the exit, listing the present users

### Requirement: Attraction persistence
Attraction definitions (identifier, name, capacity, type) SHALL be stored in a text file and restored at startup; records without a type SHALL load as `GENERAL`. Users present SHALL NOT be persisted: every attraction starts empty.

#### Scenario: Attractions restart empty
- **GIVEN** attraction `A01` with users present
- **WHEN** the application restarts
- **THEN** `A01` exists with its capacity and type and no users present

### Requirement: Ticket kinds accepted by attraction type
`GENERAL` attractions SHALL accept individual and group tickets; `INDIVIDUAL` only individual tickets; `GROUP` only group tickets; `SPECIAL` only individual tickets. The restriction SHALL be expressed with ticket access interfaces and attraction classes using bounded generic types. A ticket of a non-accepted kind SHALL be rejected without changes.

#### Scenario: Kind mismatch
- **WHEN** a group ticket enters an `INDIVIDUAL` or `SPECIAL` attraction, or an individual ticket enters a `GROUP` attraction
- **THEN** an error is reported and nobody enters

### Requirement: SpeedPass entry
`ATTRACTION ENTER;<id>;<ticketId>;SPEED` SHALL, at `GENERAL`, `INDIVIDUAL` and `GROUP` attractions, require one unused SpeedPass on every individual ticket admitted (the ticket itself, or each constituent of a group ticket) in addition to all ordinary checks. On success it SHALL consume exactly one SpeedPass per admitted user and report the consumed pass identifiers; on any failure no user SHALL enter and no pass SHALL be consumed. A fourth parameter other than `SPEED` SHALL be rejected.

#### Scenario: Individual SPEED entry
- **GIVEN** a valid ticket with two unused SpeedPasses
- **WHEN** it enters with `SPEED`
- **THEN** exactly one SpeedPass becomes consumed and its identifier is reported

#### Scenario: Group SPEED entry is atomic
- **GIVEN** a group ticket where one constituent has no unused SpeedPass
- **WHEN** it enters with `SPEED`
- **THEN** nobody enters and no SpeedPass is consumed

### Requirement: Special attraction entry
At `SPECIAL` attractions an individual ticket SHALL enter only if it has an unused Special Pass, which SHALL be consumed after a successful entry and reported. The `SPEED` option SHALL be rejected at `SPECIAL` attractions.

#### Scenario: Special Pass consumed
- **GIVEN** a valid ticket with an unused Special Pass and a `SPECIAL` attraction
- **WHEN** the ticket enters
- **THEN** the user is present, the Special Pass is consumed and reported, and a second entry after exiting is rejected

#### Scenario: SPEED rejected
- **WHEN** `ATTRACTION ENTER;<special>;<ticket>;SPEED` is executed
- **THEN** an error is reported and no pass is consumed
