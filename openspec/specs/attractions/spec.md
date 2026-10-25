# attractions Specification

## Purpose

Manages park attractions and lets visitors enter and leave them with individual or group tickets, respecting capacity and date validity, and reports their current state.

## Requirements

### Requirement: Create and delete attractions
`ATTRACTION CREATE;Id;Nombre;Capacidad[;Tipo]` SHALL create an attraction with a unique identifier, a name, a positive integer maximum capacity and a type among `GENERAL`, `INDIVIDUAL`, `GROUP` and `SPECIAL`; without a type it SHALL be `GENERAL`, and unknown types SHALL be rejected. `ATTRACTION DELETE;Id` SHALL delete an existing attraction that has nobody inside. Attraction definitions, including their type, SHALL persist between executions, and every attraction SHALL be empty when the application starts.

#### Scenario: Create
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is executed
- **THEN** A01 exists with capacity 20, type GENERAL and no users present

#### Scenario: Create with type
- **WHEN** `ATTRACTION CREATE;A02;Noria;10;GROUP` is executed and the application restarts
- **THEN** A02 exists with type GROUP

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A02;Noria;0` or `ATTRACTION CREATE;A02;Noria;x` is executed
- **THEN** an error is reported

#### Scenario: Restart empties attractions
- **WHEN** users are inside A01 and the application restarts
- **THEN** A01 exists and has no users present

### Requirement: Enter with a ticket
`ATTRACTION ENTER;Id;Ticket[;SPEED]` SHALL add to the attraction the user of an individual ticket, or all users of the individual tickets composing a group ticket, handling both ticket kinds uniformly, provided the attraction type admits the ticket kind. The ticket's date SHALL equal the current date. Any optional fourth component other than `SPEED` SHALL be rejected.

#### Scenario: Individual entry
- **WHEN** the date is 2026-10-15 and `ATTRACTION ENTER;A01;T001` is executed for T001 dated 2026-10-15
- **THEN** T001's user is present in A01

#### Scenario: Group entry
- **WHEN** `ATTRACTION ENTER;A01;TG01` is executed for a valid group ticket of 3 members
- **THEN** the 3 users are present in A01

#### Scenario: Wrong date
- **WHEN** the ticket's date differs from the current date
- **THEN** entry is rejected

### Requirement: Capacity and atomic entry
Entry SHALL never make the number of users present exceed the maximum capacity. If not all users of a ticket can enter (capacity or any of them already present), none SHALL enter.

#### Scenario: Group exceeds remaining capacity
- **WHEN** 3 places remain and a group ticket with 4 users is used
- **THEN** the operation is rejected and nobody enters

### Requirement: Exit with a ticket
`ATTRACTION EXIT;Id;Ticket` SHALL remove from the attraction the users of that ticket who are present, freeing their places; it SHALL be rejected if none of them is present. The date SHALL NOT be checked on exit.

#### Scenario: Exit frees capacity
- **WHEN** T001's user is in A01 and `ATTRACTION EXIT;A01;T001` is executed
- **THEN** the user is no longer present and available places increase by one

### Requirement: Attraction status
`ATTRACTION STATUS;Id` SHALL display the identifier, name, maximum capacity, number of users present, available places and the users currently present, reflecting the state at that moment regardless of whether users entered individually or with a group ticket.

#### Scenario: Status after entries and exit
- **WHEN** T001 and TG01 (2 users) entered A01 (capacity 20) and then T001 exited
- **THEN** status shows 2 present, 18 available and lists the two group users

### Requirement: Attraction types
GENERAL attractions SHALL admit individual and group tickets, INDIVIDUAL attractions only individual tickets, GROUP attractions only group tickets, and SPECIAL attractions only individual tickets subject to Special Pass rules. Ticket access capabilities SHALL be represented with interfaces and the admitted ticket kinds restricted with bounded generic types.

#### Scenario: GROUP rejects individual ticket
- **WHEN** `ATTRACTION ENTER;A02;T001` is executed for GROUP attraction A02 and individual ticket T001
- **THEN** entry is rejected

#### Scenario: INDIVIDUAL rejects group ticket
- **WHEN** a group ticket is used at an INDIVIDUAL attraction
- **THEN** entry is rejected

### Requirement: SpeedPass entry
With the `SPEED` option at GENERAL or INDIVIDUAL attractions (individual tickets) and GENERAL or GROUP attractions (group tickets), every individual ticket involved SHALL have at least one unused SpeedPass; on successful entry exactly one unused SpeedPass per individual ticket SHALL be consumed and reported. If any member lacks one or any other entry condition fails, nobody enters and no pass is consumed.

#### Scenario: Individual SPEED
- **WHEN** T001 has two unused SpeedPasses and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly one of them is consumed and the reply names the consumed SpeedPass

#### Scenario: Group SPEED atomic
- **WHEN** a group ticket of 3 members is used with SPEED and one member's internal ticket has no unused SpeedPass
- **THEN** entry is rejected, nobody enters and no SpeedPass is consumed

### Requirement: Special Pass entry
At SPECIAL attractions an individual ticket SHALL be admitted only if it has an unconsumed Special Pass, which the system locates automatically and marks consumed only after successful entry, reporting the consumption. The `SPEED` option SHALL be rejected at SPECIAL attractions.

#### Scenario: Special entry
- **WHEN** T001 has an unused Special Pass and `ATTRACTION ENTER;A03;T001` is executed at SPECIAL attraction A03
- **THEN** the user enters, the Special Pass becomes consumed and the reply reports it

#### Scenario: No Special Pass or already used
- **WHEN** the ticket has no Special Pass or it is consumed
- **THEN** entry is rejected

#### Scenario: SPEED at SPECIAL
- **WHEN** `ATTRACTION ENTER;A03;T001;SPEED` is executed
- **THEN** entry is rejected and no pass is consumed
