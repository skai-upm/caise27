# attractions Specification

## Purpose

Defines park attractions, how users enter them with valid tickets and leave them, and how their occupancy is consulted.

## Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;Identifier;Name;Capacity;Type` SHALL create an empty attraction with a unique identifier, a name, a positive integer capacity and a type among `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`; the form without type SHALL create a `GENERAL` attraction. Duplicate identifiers, non-positive or non-numeric capacities and unknown types SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction A01 exists with capacity 20, type GENERAL and no occupants

#### Scenario: Create typed attraction
- **WHEN** `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` is processed
- **THEN** attraction A04 exists with type SPECIAL

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;abc` is processed
- **THEN** an error is printed and A03 is not created

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A05;Noria;10;VIP` is processed
- **THEN** an error is printed and A05 is not created

### Requirement: Enter attraction with a ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier[;SPEED]` SHALL let the ticket's users enter when the ticket exists, its date equals the current system date and the attraction type accepts it: `GENERAL` individual and group tickets, `INDIVIDUAL` and `SPECIAL` individual tickets only, `GROUP` group tickets only. With `SPEED` (not allowed at `SPECIAL`), every entering individual ticket SHALL have an unconsumed SpeedPass and exactly one per ticket SHALL be consumed and reported. At `SPECIAL`, the ticket's unconsumed Special Pass SHALL be consumed automatically and reported. Entry and consumption SHALL be all-or-nothing, rejected if capacity is insufficient or anyone is already inside.

#### Scenario: Individual entry
- **WHEN** T001 for Ana is dated today and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana is inside A01

#### Scenario: Group entry
- **WHEN** TG01 for Ana and Pedro is dated today and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** both Ana and Pedro are inside A01

#### Scenario: Insufficient capacity
- **WHEN** A02 has 1 free place and a two-member group ticket is used
- **THEN** an error is printed and nobody enters

#### Scenario: Ticket not valid today
- **WHEN** the ticket's date differs from the current system date
- **THEN** an error is printed and nobody enters

#### Scenario: Presence independent of later date changes
- **WHEN** Ana entered with a ticket for today and the system date is then changed
- **THEN** Ana is still inside the attraction

#### Scenario: Type restriction
- **WHEN** a group ticket is used at an `INDIVIDUAL` or `SPECIAL` attraction, or an individual ticket at a `GROUP` attraction
- **THEN** an error is printed and nobody enters

#### Scenario: Individual entry with SpeedPass
- **WHEN** T001 has unconsumed SpeedPasses and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** Ana enters, exactly one SpeedPass becomes consumed and its identifier is printed

#### Scenario: Group SpeedPass entry is atomic
- **WHEN** one composition ticket of TG01 has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** an error is printed, nobody enters and no SpeedPass is consumed

#### Scenario: Special attraction consumes Special Pass
- **WHEN** T001 has an unconsumed Special Pass and `ATTRACTION ENTER;A04;T001` is processed at SPECIAL attraction A04
- **THEN** Ana enters and the output states that the Special Pass was consumed

#### Scenario: Special attraction without valid pass
- **WHEN** T001 has no Special Pass or it is already consumed, or `SPEED` is requested at A04
- **THEN** an error is printed, nobody enters and no pass changes

### Requirement: Leave attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove that user from the attraction and free one place. A user not inside SHALL be rejected. Users who entered with a group ticket SHALL leave individually.

#### Scenario: Leave
- **WHEN** Ana is inside A01 and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** Ana is no longer inside A01 and one place is freed

### Requirement: List attraction occupants
`ATTRACTION LIST;AttractionIdentifier` SHALL print the emails of the users currently inside, or a controlled empty result, without changing the attraction.

#### Scenario: Empty attraction
- **WHEN** A01 has no occupants and `ATTRACTION LIST;A01` is processed
- **THEN** an empty result message is printed

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL print identifier, name, maximum capacity, number of users inside, available places and the users inside, from the attraction's own state and regardless of how users entered.

#### Scenario: Status after group entry
- **WHEN** A01 (capacity 20) has Ana and Pedro inside
- **THEN** the status shows A01, Dragon Rojo, capacity 20, 2 inside, 18 available, and both emails
