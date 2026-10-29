# attractions Specification

## Purpose

Defines park attractions and how users enter and leave them with tickets while respecting each attraction's capacity, plus read-only views of their occupants and status.

## Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;Identifier;Name;Capacity[;Type]` SHALL create an empty attraction with a unique, non-empty identifier, a name, a positive integer capacity and a type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL` (`GENERAL` when omitted). The attraction SHALL hold references to the users inside it; users SHALL NOT reference attractions.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction `A01` exists with name `Dragon Rojo`, capacity 20 and no occupants

#### Scenario: Invalid capacity or duplicate id
- **WHEN** `ATTRACTION CREATE` has a capacity that is not a positive integer, or an identifier that already exists
- **THEN** an error is reported and no attraction is created

#### Scenario: Create typed attractions
- **WHEN** `ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL`, `ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP` and `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` are processed
- **THEN** the attractions exist with types INDIVIDUAL, GROUP and SPECIAL, and `A01` created without a type is GENERAL

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A05;X;10;VIP` is processed
- **THEN** an error is reported and no attraction is created

### Requirement: Enter attraction with a ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier[;SPEED]` SHALL admit the users of an existing ticket whose date equals the current system date: the user of an individual ticket, or all users of a group ticket's composition. Entry SHALL be all-or-nothing and SHALL be rejected if the free places are fewer than the users to admit, if any of them is already inside that attraction, if any of them is no longer an active user, if the attraction type does not accept the ticket kind, or if the required passes are missing. On rejection no user enters and no pass changes.

#### Scenario: Individual entry
- **WHEN** the system date is 2027-04-15, Ana has ticket `T001` for 2027-04-15 and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana is inside `A01`

#### Scenario: Ticket not valid today
- **WHEN** ticket `T001` is dated 2027-04-16 and the system date is 2027-04-15
- **THEN** `ATTRACTION ENTER;A01;T001` is rejected and nobody enters

#### Scenario: Group entry
- **WHEN** group ticket `TG01` for today is composed of Ana's and Pedro's tickets and `ATTRACTION ENTER;A01;TG01` is processed with at least 2 free places
- **THEN** both Ana and Pedro are inside `A01`

#### Scenario: Insufficient capacity is indivisible
- **WHEN** `A02` has 1 free place and `ATTRACTION ENTER;A02;TG01` is processed for a two-member group ticket
- **THEN** an error is reported and neither user enters

#### Scenario: Presence independent of later date changes
- **WHEN** Ana entered `A01` with a ticket for today and then the system date is changed
- **THEN** Ana is still inside `A01`

#### Scenario: Ticket kinds accepted by type
- **WHEN** entry is attempted with individual and group tickets valid today
- **THEN** GENERAL accepts both, INDIVIDUAL and SPECIAL reject group tickets, and GROUP rejects individual tickets

#### Scenario: Individual entry with SpeedPass
- **WHEN** `T001` has unconsumed SpeedPasses and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** Ana enters, exactly one of those SpeedPasses becomes consumed, and the output names it

#### Scenario: SPEED without an available SpeedPass
- **WHEN** `T001` has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error is reported and nobody enters

#### Scenario: Group entry with SpeedPasses is atomic
- **WHEN** `ATTRACTION ENTER;A03;TG01;SPEED` is processed and one composition ticket of `TG01` has no unconsumed SpeedPass (or capacity is insufficient)
- **THEN** no member enters and no SpeedPass is consumed; when every composition ticket has one and there is room, all members enter and one SpeedPass per composition ticket is consumed

#### Scenario: Special attraction consumes the Special Pass
- **WHEN** `T001` has an unconsumed Special Pass and `ATTRACTION ENTER;A04;T001` is processed at SPECIAL attraction `A04`
- **THEN** Ana enters, the Special Pass becomes consumed, and the output says so

#### Scenario: Special attraction rejections
- **WHEN** at a SPECIAL attraction the ticket has no Special Pass, its Special Pass is already consumed, `SPEED` is given, or a group ticket is used
- **THEN** an error is reported, nobody enters and no pass changes

### Requirement: Exit attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove that user from the attraction's occupants and free one place. Each user who entered with a group ticket SHALL leave individually.

#### Scenario: Exit after group entry
- **WHEN** Ana and Pedro entered `A01` with `TG01` and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** only Pedro remains inside `A01`

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT` names a user who is not inside the attraction
- **THEN** an error is reported

### Requirement: List attraction occupants
`ATTRACTION LIST;AttractionIdentifier` SHALL display the emails of the users currently inside, or an explicit empty result, without changing the attraction.

#### Scenario: List occupants
- **WHEN** Ana is inside `A01` and `ATTRACTION LIST;A01` is processed
- **THEN** the output contains `ana@correo.com`

#### Scenario: Empty attraction
- **WHEN** nobody is inside `A01` and `ATTRACTION LIST;A01` is processed
- **THEN** a controlled empty result is shown

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL display the identifier, name, maximum capacity, number of users inside, number of available places and the users inside, from the attraction's own state, regardless of how users entered.

#### Scenario: Status after mixed entries
- **WHEN** `A01` (capacity 20) contains Ana (individual ticket) and Pedro (group ticket) and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Dragon Rojo`, capacity 20, 2 inside, 18 available and both emails

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION STATUS;A99` or `ATTRACTION LIST;A99` is processed and `A99` does not exist
- **THEN** an error is reported
