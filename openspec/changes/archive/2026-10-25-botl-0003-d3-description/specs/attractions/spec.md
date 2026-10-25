# Spec Delta

## MODIFIED Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity[;Type]` SHALL create an empty attraction with a unique identifier, a non-blank name, a positive integer capacity (maximum simultaneous occupants) and a type among `GENERAL`, `INDIVIDUAL`, `GROUP` and `SPECIAL` (case-insensitive). Without a type the attraction SHALL be `GENERAL`. Invalid or duplicate data SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** A01 exists with capacity 20 and no occupants

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;abc` is processed
- **THEN** an error is printed and A03 is not created

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` is processed
- **THEN** A04 exists as a SPECIAL attraction

#### Scenario: Invalid type
- **WHEN** `ATTRACTION CREATE;A05;Noria;10;VIP` is processed
- **THEN** an error is printed and A05 is not created

### Requirement: Entering with a ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier[;SPEED]` SHALL let the users of an existing ticket dated exactly the current system date enter the attraction, if the attraction's type accepts that kind of ticket: the ticket's user for an individual ticket, all users of its individual tickets for a group ticket. Entry SHALL be all-or-nothing: if capacity is insufficient for everyone, any of them is already inside, or any pass condition fails, nobody enters and no pass changes. The date SHALL be checked only at entry.

#### Scenario: Individual entry
- **WHEN** the system date is 2027-04-15 and `ATTRACTION ENTER;A02;T001` is processed for Ana's ticket dated 2027-04-15
- **THEN** Ana is inside A02

#### Scenario: Group entry
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed for a valid group ticket of 3 users and A01 has at least 3 free places
- **THEN** all 3 users are inside A01

#### Scenario: Insufficient capacity
- **WHEN** a group ticket of 3 users is used on an attraction with 2 free places
- **THEN** an error is printed and none of them enters

#### Scenario: Ticket for another day
- **WHEN** a ticket dated 2027-04-20 is used while the system date is 2027-04-15
- **THEN** an error is printed and nobody enters

#### Scenario: Presence survives date change
- **WHEN** Ana is inside A02 and the system date is then changed
- **THEN** Ana is still inside A02

## ADDED Requirements

### Requirement: Accepted ticket kinds per attraction type
`GENERAL` attractions SHALL accept individual and group tickets; `INDIVIDUAL` and `SPECIAL` attractions SHALL accept individual tickets only (including individual tickets inside a group ticket); `GROUP` attractions SHALL accept group tickets only. Other ticket kinds SHALL be rejected.

#### Scenario: Group ticket at individual attraction
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed and A02 is INDIVIDUAL
- **THEN** an error is printed and nobody enters

#### Scenario: Individual ticket at group attraction
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed and A03 is GROUP
- **THEN** an error is printed and nobody enters

#### Scenario: Group ticket at special attraction
- **WHEN** `ATTRACTION ENTER;A04;TG01` is processed and A04 is SPECIAL
- **THEN** an error is printed and nobody enters

### Requirement: Entry with SpeedPass
With `;SPEED`, at `GENERAL`, `INDIVIDUAL` and `GROUP` attractions every individual ticket entering (the ticket itself, or each individual ticket of a group ticket) SHALL have an unconsumed SpeedPass; on success exactly one SpeedPass per individual ticket SHALL be marked consumed and the output SHALL name the consumed passes. Without `;SPEED`, no SpeedPass SHALL be consumed. `SPECIAL` attractions SHALL reject `;SPEED`. Any other final argument SHALL be rejected.

#### Scenario: Individual speed entry
- **WHEN** T001 has ten unconsumed SpeedPasses and `ATTRACTION ENTER;A02;T001;SPEED` succeeds
- **THEN** exactly one of them is consumed and its identifier is shown

#### Scenario: Group speed entry
- **WHEN** each of TG01's three individual tickets has one unconsumed SpeedPass and `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** all three members enter and each SpeedPass is consumed

#### Scenario: One member without SpeedPass
- **WHEN** one of TG01's individual tickets has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** an error is printed, nobody enters and no SpeedPass is consumed

#### Scenario: Speed at special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed and A04 is SPECIAL
- **THEN** an error is printed and no pass changes

### Requirement: Entry at special attractions
At a `SPECIAL` attraction, entry with an individual ticket SHALL require an unconsumed Special Pass on that ticket, found automatically; on success the Special Pass SHALL be marked consumed and the output SHALL say so. Without such a pass, entry SHALL be rejected without changing any pass.

#### Scenario: Special entry
- **WHEN** T001 has unconsumed Special Pass PE01 and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** Ana enters A04 and the output states that PE01 was consumed

#### Scenario: Special pass already consumed
- **WHEN** PE01 is consumed and `ATTRACTION ENTER;A04;T001` is processed again
- **THEN** an error is printed and nobody enters
