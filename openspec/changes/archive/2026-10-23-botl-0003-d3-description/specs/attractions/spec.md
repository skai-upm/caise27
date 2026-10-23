# Spec Delta

## MODIFIED Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity;Type` SHALL create an empty attraction of type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL` (case-insensitive) when the identifier is unused, the name is non-empty and the capacity is a positive whole number; the form without a type SHALL create a `GENERAL` attraction; on success it SHALL print `ATTRACTION CREATED`.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** A01 exists with capacity 20 and no occupants

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;diez` is processed
- **THEN** an error is printed and no attraction is created

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` is processed
- **THEN** A04 exists as a SPECIAL attraction with capacity 15

#### Scenario: Default type
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** A01 is a GENERAL attraction

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A05;Noria;10;VIP` is processed
- **THEN** an error is printed and no attraction is created

### Requirement: Entry with a ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL admit the users of an existing ticket dated exactly the current system date: the ticket's user for an individual ticket, or all users of the composing tickets for a group ticket; entry SHALL be all-or-nothing and rejected when free places are insufficient, when any of those users is already inside an attraction, or when the attraction type does not accept that ticket type, and a rejected entry SHALL NOT change any pass.

#### Scenario: Individual entry
- **WHEN** T001 is Ana's ticket for today and `ATTRACTION ENTER;A02;T001` is processed
- **THEN** Ana is inside A02

#### Scenario: Group entry
- **WHEN** TG01 is a group ticket for today with three members and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** all three members are inside A01

#### Scenario: Insufficient capacity
- **WHEN** a group ticket with three members is used on an attraction with two free places
- **THEN** an error is printed and nobody enters

#### Scenario: Ticket not valid today
- **WHEN** `ATTRACTION ENTER` uses a ticket dated another day, or an unknown ticket
- **THEN** an error is printed and nobody enters

#### Scenario: Wrong ticket type
- **WHEN** a group ticket is used at an INDIVIDUAL or SPECIAL attraction, or an individual ticket at a GROUP attraction
- **THEN** an error is printed and nobody enters

## ADDED Requirements

### Requirement: Accepted ticket types
GENERAL attractions SHALL accept individual and group tickets; INDIVIDUAL and SPECIAL attractions SHALL accept individual tickets only (including tickets composing a group ticket); GROUP attractions SHALL accept group tickets only.

#### Scenario: Group ticket at GROUP attraction
- **WHEN** TG01 is valid today and `ATTRACTION ENTER;A03;TG01` is processed at a GROUP attraction
- **THEN** all members of TG01 enter

### Requirement: Entry with SpeedPass
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier;SPEED` SHALL, at GENERAL, INDIVIDUAL and GROUP attractions, require at least one unconsumed SpeedPass for each individual ticket that enters (the ticket itself, or every composing ticket of a group ticket) and, only if the whole entry succeeds, mark exactly one SpeedPass per entering ticket as consumed and print the identifiers consumed; SPECIAL attractions SHALL reject the SPEED option; any value other than `SPEED` in the fourth field SHALL be rejected.

#### Scenario: Individual SpeedPass entry
- **WHEN** T001 has ten unconsumed SpeedPasses and `ATTRACTION ENTER;A02;T001;SPEED` is processed
- **THEN** Ana enters, exactly one of them is consumed, and the output names it

#### Scenario: Group SpeedPass entry
- **WHEN** each composing ticket of TG01 has an unconsumed SpeedPass and `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** all members enter and exactly one SpeedPass per composing ticket is consumed

#### Scenario: One member without SpeedPass
- **WHEN** one composing ticket of TG01 has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** an error is printed, nobody enters and no SpeedPass is consumed

#### Scenario: SPEED at special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed at a SPECIAL attraction
- **THEN** an error is printed and nothing changes

### Requirement: Special attraction entry
At a SPECIAL attraction, `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL require that the individual ticket has an unconsumed Special Pass, mark it consumed only when entry succeeds, and report that it was consumed.

#### Scenario: Special pass consumed
- **WHEN** T001 has unconsumed Special Pass PE01 and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** Ana enters A04, PE01 is consumed and the output says so

#### Scenario: No usable special pass
- **WHEN** the ticket has no Special Pass or it is already consumed
- **THEN** an error is printed, nobody enters and the pass is unchanged
