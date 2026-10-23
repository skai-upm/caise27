# attractions Specification

## Purpose

Defines park attractions with a maximum capacity, how users enter them with tickets valid today, leave them, and how their occupancy is consulted.

## Requirements

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

### Requirement: Presence independent of date changes
Once inside, a user's presence SHALL NOT depend on later changes of the system date.

#### Scenario: Date advances
- **WHEN** Ana is inside A01 and `SYSTEM DATE` moves to the next day
- **THEN** Ana is still inside A01

### Requirement: Leaving an attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove that user from the attraction's occupants, freeing one place; users who entered with a group ticket SHALL leave individually; exiting a user who is not inside SHALL be rejected.

#### Scenario: Exit
- **WHEN** Ana is inside A01 and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** Ana is no longer inside A01 and one more place is available

#### Scenario: Not inside
- **WHEN** `ATTRACTION EXIT;A01;marta@correo.com` is processed and Marta is not inside A01
- **THEN** an error is printed

### Requirement: Occupant list
`ATTRACTION LIST;AttractionIdentifier` SHALL print the email of each user currently inside, or a message stating there are none, without changing state.

#### Scenario: Empty attraction
- **WHEN** `ATTRACTION LIST;A02` is processed and nobody is inside
- **THEN** a message stating there are no users is printed

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL print the identifier, name, maximum capacity, number of users inside, number of available places, and the users inside, regardless of how they entered.

#### Scenario: Status after group entry
- **WHEN** three users entered A01 (capacity 20) with a group ticket and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows A01, Dragon Rojo, capacity 20, 3 inside, 17 available and the three emails

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
