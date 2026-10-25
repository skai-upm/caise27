# Spec Delta

## MODIFIED Requirements

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

### Requirement: Attractions persist empty
Attractions (identifier, name, capacity and type) SHALL be stored in a `;`-separated text file and restored when the application starts; they SHALL always start empty.

#### Scenario: Restart
- **WHEN** A01 had users inside and the application restarts
- **THEN** A01 exists with capacity 20 and nobody inside

#### Scenario: Type preserved
- **WHEN** A03 is a GROUP attraction and the application restarts
- **THEN** A03 is still a GROUP attraction

## ADDED Requirements

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
