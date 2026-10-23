# attractions Specification

## Purpose

Models the park's attractions, their capacity and the users currently inside them, and lets ticket holders enter and leave.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity[;Type]` SHALL create an attraction with a non-empty identifier unique among attractions (ignoring case), a non-empty name, a positive integer capacity and a type among `GENERAL`, `INDIVIDUAL`, `GROUP` and `SPECIAL` (ignoring case), initially with no users inside. Without a type the attraction is `GENERAL`.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** `ATTRACTION CREATED` is printed and A01 exists with capacity 20 and no occupants

#### Scenario: Invalid capacity or duplicate
- **WHEN** the capacity is `0`, `-5` or `abc`, or an attraction with that identifier exists
- **THEN** an error is printed and no attraction is created

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` is processed
- **THEN** A04 exists with type SPECIAL

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A05;X;10;VIP` is processed
- **THEN** an error listing the valid types is printed and no attraction is created

### Requirement: Entering an attraction
`ATTRACTION ENTER;Attraction;Ticket[;SPEED]` SHALL let the ticket's holders enter when the attraction and ticket exist, the ticket's date equals the current system date, the attraction type accepts the ticket type, and the pass conditions hold. An individual ticket admits its user; a group ticket admits every user of its member tickets. Entry SHALL be all-or-nothing: if the free places are fewer than the people entering, any of them is already inside an attraction, or any condition fails, nobody enters and no pass changes. Later date changes SHALL NOT affect users already inside.

#### Scenario: Individual entry
- **WHEN** T001 is Ana's ticket for today and `ATTRACTION ENTER;A02;T001` is processed
- **THEN** a confirmation is printed and Ana is inside A02

#### Scenario: Group entry
- **WHEN** TG01 has three member tickets for today and `ATTRACTION ENTER;A01;TG01` is processed with 20 free places
- **THEN** all three users are inside A01

#### Scenario: Insufficient capacity
- **WHEN** an attraction has 2 free places and a group ticket with 3 members is used
- **THEN** an error is printed and none of the three users enters

#### Scenario: Ticket not valid today
- **WHEN** the ticket's date differs from the system date
- **THEN** an error is printed and nobody enters

#### Scenario: Unknown ticket or attraction
- **WHEN** the ticket or the attraction does not exist
- **THEN** an error is printed

#### Scenario: Invalid option
- **WHEN** the fourth element is anything other than `SPEED`
- **THEN** an error is printed and nobody enters

### Requirement: Leaving an attraction
`ATTRACTION EXIT;Attraction;Email` SHALL remove the user with that email from the attraction's occupants and free their place, regardless of how they entered. A user not inside SHALL produce an error.

#### Scenario: Leave
- **WHEN** Ana is inside A01 and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** a confirmation is printed and A01 has one more free place

#### Scenario: Not inside
- **WHEN** Ana is not inside A01 and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** an error is printed

### Requirement: Listing occupants
`ATTRACTION LIST;Attraction` SHALL print the email of every user currently inside, or a message that it is empty, without changing the attraction.

#### Scenario: List occupants
- **WHEN** Ana and Pedro are inside A01 and `ATTRACTION LIST;A01` is processed
- **THEN** `ana@correo.com` and `pedro@correo.com` are printed

#### Scenario: Empty attraction
- **WHEN** nobody is inside A02 and `ATTRACTION LIST;A02` is processed
- **THEN** a message that the attraction is empty is printed

### Requirement: Attraction status
`ATTRACTION STATUS;Attraction` SHALL print the identifier, name, maximum capacity, number of users inside, number of free places and the users inside.

#### Scenario: Status
- **WHEN** three users are inside A01 (capacity 20) and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows A01, `Dragon Rojo`, capacity 20, 3 inside, 17 available, and the three users

### Requirement: Ticket types accepted by attraction type
`GENERAL` attractions SHALL accept individual and group tickets; `INDIVIDUAL` and `SPECIAL` attractions SHALL accept only individual tickets (including member tickets of a group ticket); `GROUP` attractions SHALL accept only group tickets.

#### Scenario: Group ticket at individual attraction
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed and A02 is INDIVIDUAL
- **THEN** an error is printed and nobody enters

#### Scenario: Individual ticket at group attraction
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed and A03 is GROUP
- **THEN** an error is printed and nobody enters

### Requirement: Entry with SpeedPass
With `SPEED`, at `GENERAL`, `INDIVIDUAL` and `GROUP` attractions, every individual ticket entering (the ticket itself, or each member ticket of a group ticket) SHALL have an unconsumed SpeedPass. On successful entry exactly one unconsumed SpeedPass per individual ticket SHALL be marked consumed and the consumed pass identifiers SHALL be printed; on failure no pass changes.

#### Scenario: Individual SpeedPass entry
- **WHEN** T001 has unconsumed SpeedPasses and `ATTRACTION ENTER;A02;T001;SPEED` is processed
- **THEN** Ana enters and the output names the one SpeedPass that was consumed

#### Scenario: Group SpeedPass entry
- **WHEN** each of TG01's three member tickets has one unconsumed SpeedPass and `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** all three enter and exactly one SpeedPass per member ticket is consumed

#### Scenario: One member without SpeedPass
- **WHEN** one member ticket of TG01 has no unconsumed SpeedPass
- **THEN** an error is printed, nobody enters and no SpeedPass is consumed

#### Scenario: No SpeedPass
- **WHEN** T001 has no unconsumed SpeedPass and `SPEED` is used
- **THEN** an error is printed and nobody enters

### Requirement: Entry to special attractions
At `SPECIAL` attractions the individual ticket SHALL have an unconsumed Special Pass, which SHALL be found and consumed automatically on successful entry, and the output SHALL say that it was consumed. `SPEED` SHALL be rejected at special attractions.

#### Scenario: Special Pass consumed
- **WHEN** T001 has unconsumed Special Pass PE01 and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** Ana enters A04 and the output states that PE01 was consumed

#### Scenario: Missing or consumed Special Pass
- **WHEN** T001 has no Special Pass or it is already consumed
- **THEN** an error is printed and nobody enters

#### Scenario: SPEED at special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an error is printed and no pass changes
