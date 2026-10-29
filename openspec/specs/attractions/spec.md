# attractions Specification

## Purpose

Defines park attractions, their capacity-limited occupancy, entry with individual or group tickets, exit, and read-only listing and status queries.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with a unique non-empty identifier, a non-empty name, a positive integer maximum capacity, and an empty collection of occupants. The attraction SHALL reference its occupants, and users SHALL NOT reference attractions. The optional fifth field `<type>` SHALL be one of `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`. When it is omitted, the attraction is `GENERAL`.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction A01 exists with capacity 20 and no occupants

#### Scenario: Invalid capacity or duplicate
- **WHEN** `ATTRACTION CREATE;A02;X;0`, `ATTRACTION CREATE;A02;X;abc`, or a second `ATTRACTION CREATE;A01;...` is processed
- **THEN** an error is printed and no attraction is created

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` is processed
- **THEN** A04 is a SPECIAL attraction

#### Scenario: Invalid type
- **WHEN** `ATTRACTION CREATE;A05;X;10;VIP` is processed
- **THEN** an error is printed and no attraction is created

### Requirement: Entry with a ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier[;SPEED]` SHALL admit the ticket's users when the attraction and ticket exist, the ticket's date equals the current system date, and the attraction's type accepts the ticket. An individual ticket admits its user; a group ticket admits every composition user. Entry SHALL be all-or-nothing: if capacity is insufficient, any user is already inside, or a pass condition fails, nobody enters and no pass changes.

#### Scenario: Individual entry
- **WHEN** T001 is dated today and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** T001's user is inside A01

#### Scenario: Group entry
- **WHEN** TG01 for 3 users is dated today and `ATTRACTION ENTER;A01;TG01` is processed with at least 3 free places
- **THEN** all 3 users are inside A01

#### Scenario: Insufficient capacity
- **WHEN** A02 has 2 free places and `ATTRACTION ENTER;A02;TG01` is processed for a 3-user group ticket
- **THEN** an error is printed and no user enters

#### Scenario: Ticket not valid today
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed and T001's date differs from the system date
- **THEN** an error is printed and nobody enters

#### Scenario: Presence independent of later date changes
- **WHEN** a user entered A01 and the system date is then changed
- **THEN** the user remains inside A01

### Requirement: Leaving an attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the user with that email from the attraction's occupants and free one place, regardless of how they entered. An unknown attraction, or a user who is not inside, SHALL produce an error.

#### Scenario: Group member leaves individually
- **WHEN** three users entered A01 with TG01 and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** Ana is no longer inside and the other two remain

### Requirement: Attraction list
`ATTRACTION LIST;AttractionIdentifier` SHALL display the emails of the users currently inside the attraction, or an explicit empty result, without changing any state.

#### Scenario: Empty attraction
- **WHEN** `ATTRACTION LIST;A01` is processed and A01 has no occupants
- **THEN** a controlled empty result is printed

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL display, from the attraction's own state, the identifier, name, maximum capacity, number of users inside, number of available places, and the users inside.

#### Scenario: Status after entries
- **WHEN** A01 (capacity 20) has 3 occupants and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows A01, its name, capacity 20, 3 inside, 17 available, and the 3 occupants

### Requirement: Accepted ticket types per attraction type
GENERAL attractions SHALL accept individual and group tickets; INDIVIDUAL and SPECIAL attractions SHALL accept only individual tickets; GROUP attractions SHALL accept only group tickets. Ticket access capabilities SHALL be interfaces implemented by the ticket types, and attractions SHALL be bounded generic types over those interfaces.

#### Scenario: Group ticket at INDIVIDUAL attraction
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed and A02 is INDIVIDUAL
- **THEN** an error is printed and nobody enters

#### Scenario: Individual ticket at GROUP attraction
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed and A03 is GROUP
- **THEN** an error is printed and nobody enters

### Requirement: Entry with SpeedPass
At GENERAL, INDIVIDUAL and GROUP attractions, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` SHALL require an unconsumed SpeedPass on every entering individual ticket (on the ticket itself, or on each composition ticket of a group ticket). On success it SHALL consume exactly one SpeedPass per individual ticket and print which passes were consumed. Any trailing value other than `SPEED` SHALL be rejected.

#### Scenario: Individual SpeedPass entry
- **WHEN** T001 dated today has unconsumed SpeedPass SP01:1 and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** the user enters, SP01:1 is consumed, and the output names SP01:1

#### Scenario: Missing SpeedPass
- **WHEN** T001 has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error is printed and nobody enters

#### Scenario: Atomic group SpeedPass entry
- **WHEN** one of TG01's three composition tickets has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** nobody enters and no SpeedPass is consumed

### Requirement: Special attraction entry
At SPECIAL attractions, `ATTRACTION ENTER;<attraction>;<ticket>` SHALL require that the individual ticket has an unconsumed Special Pass, consume it only on successful entry, and say so in the output. `SPEED` entry SHALL be rejected at SPECIAL attractions.

#### Scenario: Special Pass consumed
- **WHEN** T001 dated today has unconsumed Special Pass PE01 and `ATTRACTION ENTER;A04;T001` is processed at SPECIAL A04
- **THEN** the user enters, PE01 is consumed, and the output says so

#### Scenario: No or consumed Special Pass
- **WHEN** T001 has no Special Pass, or PE01 is already consumed, and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** an error is printed, nobody enters, and the pass state is unchanged

#### Scenario: SPEED at special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an error is printed and nobody enters
