## MODIFIED Requirements

### Requirement: Create and delete attractions
`ATTRACTION CREATE;Id;Name;Capacity` SHALL create an attraction with a unique identifier (letters, digits, `-`, `_`), a non-empty name, a positive integer maximum capacity and no users present. `ATTRACTION DELETE;Id` SHALL delete an attraction that has no users present. Invalid or duplicate values, unknown attractions and deleting an occupied attraction SHALL be rejected. An optional fifth component `Type` (`GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`) SHALL select the attraction type; without it the attraction SHALL be GENERAL. Unknown types SHALL be rejected.

#### Scenario: Attraction created
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is executed
- **THEN** attraction A01 named Tornado with capacity 20 and no users present exists

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A02;Noria;0` or `ATTRACTION CREATE;A02;Noria;diez` is executed
- **THEN** an error is shown and no attraction is created

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A03;Casa Magica;10;SPECIAL` is executed
- **THEN** A03 is a SPECIAL attraction, and `ATTRACTION CREATE;A04;X;10;OTHER` is rejected

### Requirement: Enter attraction
`ATTRACTION ENTER;Id;TicketId` SHALL add to the attraction the users represented by the ticket (the ticket's user for an individual ticket; the users of all its individual tickets for a group ticket), handled uniformly for both ticket types. The ticket's date SHALL equal the system date. Entry SHALL be all-or-nothing: if capacity is insufficient for all of the ticket's users, or any of them is already present, no one enters. The ticket kind SHALL be one the attraction type admits.

#### Scenario: Individual entry
- **WHEN** T001 is dated on the system date and `ATTRACTION ENTER;A01;T001` is executed
- **THEN** T001's user is present in A01

#### Scenario: Group entry
- **WHEN** TG01 with two members is dated on the system date and `ATTRACTION ENTER;A01;TG01` is executed
- **THEN** both members are present in A01

#### Scenario: Wrong date
- **WHEN** a ticket dated other than the system date is used to enter
- **THEN** an error is shown and no one enters

#### Scenario: Insufficient capacity for group
- **WHEN** three places remain and `ATTRACTION ENTER;A01;TG01` is executed for a group ticket with four users
- **THEN** the entry is rejected and no member enters

### Requirement: Attraction persistence
Attractions (identifier, name, capacity, type) SHALL be stored in a text file next to the users file and restored at startup. Users present SHALL NOT be persisted; every attraction starts empty. Stored attractions without a type SHALL be restored as GENERAL.

#### Scenario: Attractions restored empty
- **WHEN** A01 had users present and the application restarts
- **THEN** A01 exists with the same name and capacity and no users present

#### Scenario: Type preserved
- **WHEN** a SPECIAL attraction is stored and the application restarts
- **THEN** it is restored as SPECIAL

## ADDED Requirements

### Requirement: Attraction types
GENERAL attractions SHALL admit individual and group tickets; INDIVIDUAL SHALL admit only individual tickets; GROUP SHALL admit only group tickets; SPECIAL SHALL admit only individual tickets and apply the Special Pass rules. Ticket access capabilities SHALL be represented by an interface and the accepted ticket kind by a bounded generic type parameter of the attraction.

#### Scenario: GROUP rejects individual tickets
- **WHEN** an individual ticket is used at a GROUP attraction
- **THEN** the entry is rejected

#### Scenario: INDIVIDUAL rejects group tickets
- **WHEN** a group ticket is used at an INDIVIDUAL attraction
- **THEN** the entry is rejected

### Requirement: SpeedPass entry
`ATTRACTION ENTER;Id;TicketId;SPEED` SHALL be accepted at GENERAL and INDIVIDUAL attractions for individual tickets and at GENERAL and GROUP attractions for group tickets. It SHALL consume exactly one unused SpeedPass of each admitted individual ticket and report it. If any admitted ticket lacks an unused SpeedPass, or any other entry condition fails, no one SHALL enter and no pass SHALL be consumed. Any fourth component other than `SPEED` SHALL be rejected.

#### Scenario: Individual SPEED entry
- **WHEN** T001 has two unused SpeedPasses and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly one of them is consumed and the output reports it

#### Scenario: Group SPEED entry is atomic
- **WHEN** one member's ticket in TG01 has no unused SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is executed
- **THEN** no member enters and no SpeedPass is consumed

### Requirement: Special attraction entry
At a SPECIAL attraction, entry with an individual ticket SHALL require that ticket's Special Pass to exist and be unused, and SHALL mark it consumed only after a successful entry, reporting it. `SPEED` SHALL be rejected at SPECIAL attractions.

#### Scenario: Special Pass consumed
- **WHEN** T001 has an unused Special Pass and `ATTRACTION ENTER;A03;T001` succeeds at SPECIAL attraction A03
- **THEN** the Special Pass becomes consumed and the output reports it

#### Scenario: Missing or used Special Pass
- **WHEN** T001 has no Special Pass, or it is already consumed, and entry to a SPECIAL attraction is attempted
- **THEN** the entry is rejected

#### Scenario: SPEED rejected
- **WHEN** `ATTRACTION ENTER;A03;T001;SPEED` is executed at a SPECIAL attraction
- **THEN** the entry is rejected and no pass is consumed
