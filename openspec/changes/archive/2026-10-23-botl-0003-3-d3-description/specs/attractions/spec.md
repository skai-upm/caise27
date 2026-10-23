# Spec Delta

## MODIFIED Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;<identifier>;<name>;<capacity>[;<type>]` SHALL create an empty attraction with a unique non-empty identifier, a non-empty name, a positive integer capacity, and a type. The type is `GENERAL`, `INDIVIDUAL`, `GROUP`, or `SPECIAL` (case-insensitive); when omitted it is `GENERAL`. It SHALL persist the attraction and print `ATTRACTION CREATED`. Duplicates and invalid values SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** `ATTRACTION CREATED` is printed and `A01` exists with capacity 20, type `GENERAL`, and no occupants

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;diez` is processed
- **THEN** an error is printed and `A03` is not created

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` is processed
- **THEN** `A04` exists with type `SPECIAL`

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A05;Noria;10;VIP` is processed
- **THEN** an error is printed and `A05` is not created

### Requirement: Entering with a ticket
`ATTRACTION ENTER;<attraction>;<ticket>[;SPEED]` SHALL let every holder of the ticket enter together: the ticket's user, or each member of a group ticket. The ticket SHALL exist, be dated today, and be accepted by the attraction's type. Every holder SHALL be an active user not inside any attraction, capacity SHALL suffice, and the required passes SHALL be available. Otherwise nobody enters, no pass changes, and an error is printed. On success it SHALL print `ATTRACTION ENTERED` with the number of users and any consumed passes.

#### Scenario: Enter with individual ticket
- **WHEN** ticket `T001` for Ana is dated today and `ATTRACTION ENTER;A02;T001` is processed
- **THEN** Ana is inside `A02`

#### Scenario: Enter with group ticket
- **WHEN** group ticket `TG01` for Ana, Pedro, and Laura is dated today and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** all three users are inside `A01`

#### Scenario: Insufficient capacity is all-or-nothing
- **WHEN** an attraction with 2 free places is entered with a group ticket for 3 users
- **THEN** an error is printed and none of the 3 users enters

#### Scenario: Ticket not valid today
- **WHEN** `ATTRACTION ENTER;A01;T002` is processed for a ticket dated on another day
- **THEN** an error is printed and nobody enters

#### Scenario: Unsupported option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is processed
- **THEN** an error is printed and nobody enters

### Requirement: Attraction status
`ATTRACTION STATUS;<attraction>` SHALL print the attraction's identifier, name, type, maximum capacity, number of occupants, number of free places, and occupants' emails. The values SHALL be derived from the attraction's own state, whichever way the users entered.

#### Scenario: Status after group entry and one exit
- **WHEN** 3 users entered `A01` (capacity 20) with a group ticket and one has left
- **THEN** the status shows `A01`, `Dragon Rojo`, type `GENERAL`, capacity 20, 2 occupants, 18 free places, and the two remaining emails

## ADDED Requirements

### Requirement: Tickets accepted by attraction type
`GENERAL` attractions SHALL accept individual and group tickets. `INDIVIDUAL` and `SPECIAL` attractions SHALL accept individual tickets only, including an individual ticket inside a group ticket. `GROUP` attractions SHALL accept group tickets only. A ticket of a kind the type does not accept SHALL be rejected with an error.

#### Scenario: Group ticket at individual attraction
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed and `A02` is `INDIVIDUAL`
- **THEN** an error is printed and nobody enters

#### Scenario: Individual ticket at group attraction
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed and `A03` is `GROUP`
- **THEN** an error is printed and nobody enters

#### Scenario: Group ticket at special attraction
- **WHEN** `ATTRACTION ENTER;A04;TG01` is processed and `A04` is `SPECIAL`
- **THEN** an error is printed and nobody enters

### Requirement: Entry with SpeedPass
With the `SPEED` option, at `GENERAL`, `INDIVIDUAL`, and `GROUP` attractions, every individual ticket entering (the ticket itself, or each ticket of a group ticket) SHALL have an unconsumed SpeedPass. On success, exactly one SpeedPass per entering individual ticket SHALL be marked consumed and persisted, and the output SHALL name the consumed passes. If any check fails, nobody enters and no SpeedPass changes.

#### Scenario: Individual speed entry
- **WHEN** `T001` has ten unconsumed SpeedPasses and `ATTRACTION ENTER;A02;T001;SPEED` is processed
- **THEN** Ana enters, exactly one of `T001`'s SpeedPasses is consumed, and its identifier is printed

#### Scenario: Group speed entry
- **WHEN** each of the three tickets of `TG01` has one unconsumed SpeedPass and `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** all three members enter and exactly one SpeedPass of each ticket is consumed

#### Scenario: One member without SpeedPass
- **WHEN** one ticket of `TG01` has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** an error is printed, nobody enters, and no SpeedPass is consumed

#### Scenario: Speed entry without passes
- **WHEN** `ATTRACTION ENTER;A01;T009;SPEED` is processed and `T009` has no unconsumed SpeedPass
- **THEN** an error is printed and nobody enters

### Requirement: Entry to special attractions
A `SPECIAL` attraction SHALL require the entering individual ticket to have an unconsumed Special Pass. That pass SHALL be found automatically without any extra option. It SHALL be marked consumed and persisted only when entry succeeds, and the output SHALL state that it was consumed. The `SPEED` option SHALL be rejected at `SPECIAL` attractions.

#### Scenario: Special entry consumes the pass
- **WHEN** `T001` has unconsumed Special Pass `PE01` and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** Ana enters, `PE01` is consumed, and the output mentions `PE01`

#### Scenario: Missing or consumed Special Pass
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed and `T001` has no Special Pass, or it is already consumed
- **THEN** an error is printed and nobody enters

#### Scenario: SPEED at special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an error is printed, nobody enters, and no pass changes
