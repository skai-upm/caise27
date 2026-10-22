# attractions Specification

## Purpose

Defines park attractions with a limited capacity, how users enter them with tickets and leave them, and how their occupancy is consulted.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;<Identifier>;<Name>;<Capacity>[;<Type>]` SHALL create an empty attraction when the identifier is unused, the name is non-empty and the capacity is a positive integer. The type SHALL be one of `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL` (case-insensitive); without it the attraction SHALL be `GENERAL`.

#### Scenario: Create an attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction A01 exists with capacity 20, type GENERAL and no occupants

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;abc` is processed
- **THEN** an error is printed and no attraction is created

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` is processed
- **THEN** attraction A04 of type SPECIAL exists

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A05;Noria;10;VIP` is processed
- **THEN** an error is printed and no attraction is created

### Requirement: Entering with a ticket
`ATTRACTION ENTER;<Attraction>;<Ticket>[;SPEED]` SHALL admit the users of an existing ticket whose date equals the system date and whose kind the attraction type accepts: `GENERAL` accepts individual and group tickets, `INDIVIDUAL` and `SPECIAL` only individual tickets, `GROUP` only group tickets. An individual ticket admits its user; a group ticket admits every user of its individual tickets. Entry SHALL be all-or-nothing: if any check fails (type, date, free places, a user already inside an attraction, required passes), nobody enters and no pass changes. Later date changes SHALL NOT affect users already inside.

#### Scenario: Individual entry
- **WHEN** `ATTRACTION ENTER;A02;T001` is processed and T001 is Ana's ticket for the system date
- **THEN** Ana is inside A02

#### Scenario: Group entry
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed and TG01 is a three-member group ticket for the system date
- **THEN** the three members are inside A01

#### Scenario: Not enough capacity
- **WHEN** a three-member group ticket is used on an attraction with two free places
- **THEN** an error is printed and nobody enters

#### Scenario: Ticket for another date
- **WHEN** `ATTRACTION ENTER` uses a ticket whose date is not the system date
- **THEN** an error is printed and nobody enters

#### Scenario: Group ticket at an individual attraction
- **WHEN** a group ticket is used at an `INDIVIDUAL` or `SPECIAL` attraction
- **THEN** an error is printed and nobody enters

#### Scenario: Individual ticket at a group attraction
- **WHEN** an individual ticket is used at a `GROUP` attraction
- **THEN** an error is printed and nobody enters

### Requirement: Leaving an attraction
`ATTRACTION EXIT;<Attraction>;<Email>` SHALL remove the user with that email from the attraction's occupants, releasing a place; users who entered with a group ticket SHALL leave individually.

#### Scenario: Leave
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` is processed and Ana is inside A01
- **THEN** Ana is no longer inside A01 and one more place is free

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT` names a user who is not inside the attraction
- **THEN** an error is printed

### Requirement: Listing occupants
`ATTRACTION LIST;<Attraction>` SHALL print the email of every user currently inside, or an explicit empty result, without changing the attraction.

#### Scenario: Empty attraction
- **WHEN** `ATTRACTION LIST;A02` is processed and nobody is inside
- **THEN** an empty result is printed

### Requirement: Attraction status
`ATTRACTION STATUS;<Attraction>` SHALL print the identifier, name, maximum capacity, number of users inside, number of free places and the users inside, regardless of how they entered.

#### Scenario: Status after group entry
- **WHEN** three users entered A01 (capacity 20) with a group ticket and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows A01, Dragon Rojo, capacity 20, 3 inside, 17 free and the three emails

### Requirement: Entry with SpeedPasses
With the `SPEED` option, `GENERAL`, `INDIVIDUAL` and `GROUP` attractions SHALL require an unconsumed SpeedPass for every individual ticket being admitted (the ticket itself, or each component of a group ticket) and, on successful entry, mark exactly one SpeedPass per such ticket as consumed and print which passes were consumed. If any ticket lacks one, nobody enters and no pass is consumed. Any other option value SHALL be rejected.

#### Scenario: Individual SpeedPass entry
- **WHEN** T001 has ten unconsumed SpeedPasses and `ATTRACTION ENTER;A02;T001;SPEED` is processed
- **THEN** Ana enters, exactly one of them is consumed and its identifier is printed

#### Scenario: Group SpeedPass entry
- **WHEN** each of TG01's three components has one unconsumed SpeedPass and `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** the three members enter and the three SpeedPasses are consumed

#### Scenario: One member without SpeedPass
- **WHEN** one component of TG01 has no unconsumed SpeedPass and `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** an error is printed, nobody enters and no SpeedPass is consumed

### Requirement: Special attraction entry
A `SPECIAL` attraction SHALL admit an individual ticket only if it has an unconsumed Special Pass, which SHALL be found automatically and marked consumed on successful entry, with the output stating it was consumed. The `SPEED` option SHALL be rejected at special attractions.

#### Scenario: Entry consumes the Special Pass
- **WHEN** T001 has unconsumed Special Pass PE01 and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** Ana enters, PE01 is consumed and the output says so

#### Scenario: No usable Special Pass
- **WHEN** the ticket has no Special Pass or it is already consumed
- **THEN** an error is printed and nobody enters

#### Scenario: SPEED at a special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an error is printed, nobody enters and no pass changes
