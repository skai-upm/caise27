# attractions Specification

## Purpose
Manages the park's attractions and lets visitors enter and leave them with their individual or group tickets, respecting capacity and the system date, and reports each attraction's current state.

## Requirements

### Requirement: Create attraction
The system SHALL create an attraction with `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo`, where `Tipo` is one of `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`, or with `ATTRACTION CREATE;Id;Nombre;Capacidad`, which creates a `GENERAL` attraction. The identifier SHALL be non-empty and unique, the name non-empty and the capacity a positive integer. A new attraction has no users present.

#### Scenario: New attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** attraction `A01` named `Tornado` with capacity 20, type `GENERAL` and no users present exists

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A02;Familiar;30;GROUP` is processed
- **THEN** attraction `A02` of type `GROUP` exists

#### Scenario: Invalid data
- **WHEN** the identifier already exists, the capacity is not a positive integer or the type is unknown
- **THEN** an error is reported and no attraction is created

### Requirement: Delete attraction
The system SHALL delete an attraction with `ATTRACTION DELETE;Id` only when no users are present in it.

#### Scenario: Delete empty attraction
- **WHEN** `ATTRACTION DELETE;A01` is processed and nobody is inside
- **THEN** `A01` no longer exists

#### Scenario: Occupied attraction
- **WHEN** users are inside `A01` and `ATTRACTION DELETE;A01` is processed
- **THEN** an error is reported and `A01` remains

### Requirement: Enter with a ticket
The system SHALL let the users of a ticket enter an attraction with `ATTRACTION ENTER;IdAtraccion;IdEntrada`. An individual ticket admits its user; a group ticket admits the users of all its individual tickets. Both kinds SHALL be handled through the same access logic.

#### Scenario: Individual entry
- **WHEN** the system date is 2026-10-15 and `ATTRACTION ENTER;A01;T001` is processed for Ana's ticket dated 2026-10-15
- **THEN** Ana is present in `A01`

#### Scenario: Group entry
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed for a valid group ticket with three individual tickets
- **THEN** the three users are present in `A01`

### Requirement: Ticket valid only on its date
Entry SHALL be rejected unless the ticket's date equals the system date.

#### Scenario: Ticket for another day
- **WHEN** the system date is 2026-10-14 and `ATTRACTION ENTER;A01;T001` is processed for a ticket dated 2026-10-15
- **THEN** an error is reported and nobody enters

### Requirement: Capacity and atomic group entry
Entry SHALL never make the number of present users exceed the capacity. If not all users of a ticket can enter, none SHALL enter. A user already present SHALL NOT enter again.

#### Scenario: Not enough places for a group
- **WHEN** three places remain and `ATTRACTION ENTER;A01;TG01` is processed for a group ticket with four users
- **THEN** the operation is rejected and no member enters

#### Scenario: Already inside
- **WHEN** Ana is present in `A01` and her ticket is used again to enter `A01`
- **THEN** an error is reported and the occupancy is unchanged

### Requirement: Leave an attraction
The system SHALL remove the ticket's users present in the attraction with `ATTRACTION EXIT;IdAtraccion;IdEntrada`, freeing their places immediately. The ticket date SHALL NOT be checked on exit. If none of the ticket's users is present, an error SHALL be reported.

#### Scenario: Exit frees capacity
- **WHEN** Ana entered `A01` with T001 and `ATTRACTION EXIT;A01;T001` is processed
- **THEN** Ana is no longer present and one more place is available

### Requirement: Attraction status
`ATTRACTION STATUS;IdAtraccion` SHALL display the identifier, name, type, maximum capacity, number of users present, number of available places and the users present, reflecting the current state regardless of whether users entered individually or as part of a group.

#### Scenario: Status after mixed entries and an exit
- **WHEN** T001 (Ana) and TG01 (Pedro, Luis) entered `A01` of capacity 20, then `ATTRACTION EXIT;A01;T001` is processed and `ATTRACTION STATUS;A01` follows
- **THEN** the status shows `A01`, `Tornado`, `GENERAL`, capacity 20, 2 present, 18 available and lists Pedro and Luis

### Requirement: Attraction persistence and empty start
Attractions SHALL be stored with identifier, name, capacity and type and restored at start-up; users present SHALL NOT be persisted, so every attraction starts empty. Stored attractions without a type SHALL be restored as `GENERAL`.

#### Scenario: Restart
- **WHEN** users are inside `A01` and the application restarts
- **THEN** `A01` exists with the same name, capacity and type and no users present

### Requirement: Attraction commands in HELP
`HELP` SHALL list the syntax of `ATTRACTION CREATE`, `ATTRACTION DELETE`, `ATTRACTION ENTER`, `ATTRACTION EXIT` and `ATTRACTION STATUS`.

#### Scenario: Help shows attraction commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the five attraction command syntaxes

### Requirement: Accepted ticket kinds per type
`GENERAL` attractions SHALL admit individual and group tickets; `INDIVIDUAL` attractions SHALL admit only individual tickets; `GROUP` attractions SHALL admit only group tickets; `SPECIAL` attractions SHALL admit only individual tickets as the basis for access, subject to the Special Pass rules. Entry with a ticket kind the type does not admit SHALL be rejected without anyone entering.

#### Scenario: GROUP rejects an individual ticket
- **WHEN** `A02` is `GROUP` and `ATTRACTION ENTER;A02;T001` is processed for an individual ticket
- **THEN** an error is reported and nobody enters

#### Scenario: INDIVIDUAL rejects a group ticket
- **WHEN** `A03` is `INDIVIDUAL` and `ATTRACTION ENTER;A03;TG01` is processed
- **THEN** an error is reported and nobody enters

#### Scenario: SPECIAL uses an individual ticket
- **WHEN** `A04` is `SPECIAL` and a valid individual ticket with an unused Special Pass is used
- **THEN** its user is admitted, while a group ticket is rejected

### Requirement: Typed access design
Ticket access capabilities SHALL be represented by at least one interface, and attractions SHALL use bounded generic types to restrict, at compile time, the ticket classes each attraction type handles.

#### Scenario: Compile-time restriction
- **WHEN** code passes a group ticket to the entry operation of an individual-only attraction
- **THEN** it does not compile, because the entry parameter type is bounded to the individual access capability

### Requirement: Entry with SpeedPass
`ATTRACTION ENTER;<atraccion>;<entrada>;SPEED` SHALL request SpeedPass use. At GENERAL and INDIVIDUAL attractions an individual ticket SHALL consume exactly one of its unused SpeedPasses. At GENERAL and GROUP attractions a group ticket SHALL require at least one unused SpeedPass on every individual ticket of its composition and SHALL consume one per member. Without SPEED, entry SHALL work as before and consume no pass.

#### Scenario: Individual SPEED entry
- **WHEN** T001 has two unused SpeedPasses and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly one of them is consumed and the response names it

#### Scenario: Group SPEED entry
- **WHEN** every ticket of TG01 has an unused SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` succeeds
- **THEN** one SpeedPass per member is consumed and all members are inside

#### Scenario: Member without SpeedPass
- **WHEN** one ticket of TG01 has no unused SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** the entry is rejected, nobody enters and no SpeedPass is consumed

#### Scenario: Normal entry keeps passes
- **WHEN** `ATTRACTION ENTER;A01;T001` succeeds for a ticket with SpeedPasses
- **THEN** no pass is consumed

### Requirement: Entry at SPECIAL attractions
At SPECIAL attractions the system SHALL locate the Special Pass of the individual ticket, reject the entry if it is missing or already consumed, and mark it consumed only after the entry succeeds. The `SPEED` option SHALL be rejected at SPECIAL attractions.

#### Scenario: Special Pass consumed
- **WHEN** T001 has an unused Special Pass and `ATTRACTION ENTER;A04;T001` succeeds at SPECIAL attraction `A04`
- **THEN** Ana enters and the response reports that the Special Pass was consumed

#### Scenario: Used or missing Special Pass
- **WHEN** the Special Pass is already consumed or does not exist
- **THEN** the entry is rejected

#### Scenario: SPEED at SPECIAL
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** the entry is rejected and no pass is consumed

### Requirement: Failed entries consume nothing
Any rejected entry (wrong ticket kind or date, capacity, user already inside, missing or used passes) SHALL leave every pass unconsumed and nobody inside.

#### Scenario: Full attraction with SPEED
- **WHEN** an attraction is full and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** the entry is rejected and T001's SpeedPasses stay unused
