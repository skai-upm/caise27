# attractions Specification

## Purpose

Models park attractions and controls ticket-based entry and exit, capacity and status queries.

## Requirements

### Requirement: Create and delete attractions
`ATTRACTION CREATE;id;name;capacity` SHALL create a GENERAL attraction and `ATTRACTION CREATE;id;name;capacity;type` SHALL create an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL, with a unique identifier, a non-empty name and a positive integer maximum capacity. `ATTRACTION DELETE;id` SHALL delete an attraction that has nobody inside.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** attraction A01 named Tornado with capacity 20 exists and is empty

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A02;Noria;0` or `ATTRACTION CREATE;A02;Noria;x` is processed
- **THEN** an `ERROR` is reported

#### Scenario: Delete occupied attraction
- **WHEN** A01 has users inside and `ATTRACTION DELETE;A01` is processed
- **THEN** an `ERROR` is reported and A01 remains

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A03;Familiar;30;GROUP` is processed
- **THEN** A03 is a GROUP attraction

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A04;X;10;VIP` is processed
- **THEN** an `ERROR` is reported

### Requirement: Enter with a ticket
`ATTRACTION ENTER;attraction;ticket` SHALL add the ticket's users to the attraction: the holder of an individual ticket, or the holders of all internal tickets of a group ticket. Both ticket kinds SHALL be handled through the same entry logic. GENERAL attractions SHALL admit individual and group tickets, INDIVIDUAL only individual tickets, GROUP only group tickets and SPECIAL only individual tickets; a ticket of a non-admitted kind SHALL be rejected without anyone entering.

#### Scenario: Individual entry
- **WHEN** T001 belongs to Ana, is dated today and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana is present in A01

#### Scenario: Group entry
- **WHEN** TG01 has tickets for Ana, Pedro and Luis, is dated today and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** Ana, Pedro and Luis are present in A01

#### Scenario: Group attraction rejects individual ticket
- **WHEN** A03 is a GROUP attraction and `ATTRACTION ENTER;A03;T001` is processed
- **THEN** an `ERROR` is reported and nobody enters

#### Scenario: Individual attraction rejects group ticket
- **WHEN** A05 is an INDIVIDUAL attraction and `ATTRACTION ENTER;A05;TG01` is processed
- **THEN** an `ERROR` is reported and nobody enters

### Requirement: Only tickets for the system date
Entry SHALL be rejected unless the ticket date equals the system date.

#### Scenario: Ticket for another day
- **WHEN** the system date is 2026-10-14 and T001 is dated 2026-10-15
- **THEN** `ATTRACTION ENTER;A01;T001` reports an `ERROR` and nobody enters

### Requirement: Capacity and atomic entry
The number of users present SHALL never exceed the maximum capacity. If not all users of a ticket fit, or any of them is already inside the attraction, none SHALL enter.

#### Scenario: Group does not fit
- **WHEN** A01 has 3 free places and `ATTRACTION ENTER;A01;TG01` is processed for a group ticket with 4 users
- **THEN** an `ERROR` is reported and none of the 4 users enters

#### Scenario: Already inside
- **WHEN** Ana is in A01 and `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** an `ERROR` is reported and occupancy is unchanged

### Requirement: Exit an attraction
`ATTRACTION EXIT;attraction;ticket` SHALL remove the ticket's users that are present and free their places; the ticket date SHALL NOT be checked. If none of the ticket's users is present an `ERROR` SHALL be reported.

#### Scenario: Exit frees capacity
- **WHEN** Ana entered A01 with T001 and `ATTRACTION EXIT;A01;T001` is processed
- **THEN** Ana is no longer present and one more place is available

### Requirement: Attraction status
`ATTRACTION STATUS;id` SHALL display the identifier, name, maximum capacity, number of users present, number of available places and the users present, reflecting the current state without distinguishing individual and group entries.

#### Scenario: Status after entries and exit
- **WHEN** T001 (Ana) and TG01 (Pedro, Luis) entered A01 with capacity 20, then `ATTRACTION EXIT;A01;T001` is processed and `ATTRACTION STATUS;A01` is processed
- **THEN** the status shows A01, Tornado, capacity 20, 2 present, 18 available, and Pedro and Luis as present

### Requirement: Attractions persist empty
Attractions (identifier, name, capacity and type) SHALL be stored in a `;`-separated text file and restored when the application starts; they SHALL always start empty.

#### Scenario: Restart
- **WHEN** A01 had users inside and the application restarts
- **THEN** A01 exists with capacity 20 and nobody inside

#### Scenario: Type preserved
- **WHEN** A03 is a GROUP attraction and the application restarts
- **THEN** A03 is still a GROUP attraction

### Requirement: SpeedPass entry
`ATTRACTION ENTER;attraction;ticket;SPEED` SHALL be accepted at GENERAL and INDIVIDUAL attractions for individual tickets and at GENERAL and GROUP attractions for group tickets, consuming exactly one unused SpeedPass of each entering individual ticket. For a group ticket every internal ticket SHALL have an unused SpeedPass; otherwise, or if any other condition fails, nobody enters and no pass is consumed. The answer SHALL report the consumed passes.

#### Scenario: Individual with SPEED
- **WHEN** T001 has two unused SpeedPasses and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** Ana enters, exactly one SpeedPass becomes consumed and its identifier is reported

#### Scenario: Group with SPEED missing a pass
- **WHEN** TG01 has three internal tickets and one of them has no unused SpeedPass
- **THEN** `ATTRACTION ENTER;A01;TG01;SPEED` reports an `ERROR`, nobody enters and no SpeedPass is consumed

#### Scenario: Group with SPEED
- **WHEN** every internal ticket of TG01 has an unused SpeedPass
- **THEN** all members enter and one SpeedPass per member is consumed

#### Scenario: Failed entry keeps passes
- **WHEN** an entry with SPEED fails for capacity
- **THEN** no SpeedPass is consumed

### Requirement: Special attraction entry
At SPECIAL attractions an individual ticket SHALL enter only if it has an unconsumed Special Pass, which SHALL be consumed automatically after a successful entry and reported. The SPEED option SHALL be rejected at SPECIAL attractions.

#### Scenario: Special Pass consumed
- **WHEN** T001 has an unused Special Pass and `ATTRACTION ENTER;S01;T001` is processed at SPECIAL attraction S01
- **THEN** Ana enters and the Special Pass is reported as consumed

#### Scenario: No usable Special Pass
- **WHEN** T001 has no Special Pass or it is already consumed
- **THEN** an `ERROR` is reported and nobody enters

#### Scenario: SPEED at special attraction
- **WHEN** `ATTRACTION ENTER;S01;T001;SPEED` is processed
- **THEN** an `ERROR` is reported and no pass is consumed
