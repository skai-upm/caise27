# attractions Specification

## Purpose

Defines park attractions, their capacity and occupants, and the commands to enter, leave, list and inspect them using tickets valid for the current date.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity;Type` SHALL create an empty attraction with a unique identifier, a name, a positive whole-number maximum capacity and a type among `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`. The form without type (`ATTRACTION CREATE;Identifier;Name;Capacity`) SHALL create a `GENERAL` attraction.

#### Scenario: Attraction created
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction A01 exists with capacity 20 and no occupants

#### Scenario: Typed attraction created
- **WHEN** `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` is processed
- **THEN** attraction A04 exists with type SPECIAL

#### Scenario: Invalid capacity or duplicate
- **WHEN** ATTRACTION CREATE uses capacity `0`, `-1` or `veinte`, an unknown type, or an existing identifier
- **THEN** an `ERROR:` message is printed and no attraction is created

### Requirement: Entering an attraction
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier[;SPEED]` SHALL let in every user holding the ticket when the attraction and ticket exist, the ticket's date equals the current system date, the attraction type accepts the ticket (GENERAL: any; INDIVIDUAL and SPECIAL: individual tickets; GROUP: group tickets), and there is room for all of them. With SPEED, each admitted individual ticket SHALL have an unconsumed SpeedPass and exactly one per ticket is consumed. SPECIAL attractions SHALL reject SPEED and SHALL require and consume the ticket's unconsumed Special Pass. Entry and consumption are all-or-nothing; consumed passes are named in the output. The ticket date is checked only at entry.

#### Scenario: Individual entry
- **WHEN** T001 for ana is dated on the current date and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** ana is inside A01

#### Scenario: Group entry together
- **WHEN** TG01 (3 members) is dated on the current date and A01 has 3 free places
- **THEN** `ATTRACTION ENTER;A01;TG01` puts all 3 members inside A01

#### Scenario: Insufficient capacity is indivisible
- **WHEN** TG01 has 3 members and A01 has 2 free places
- **THEN** `ATTRACTION ENTER;A01;TG01` prints an `ERROR:` message and nobody enters

#### Scenario: Ticket not valid today
- **WHEN** the ticket's date differs from the current system date
- **THEN** an `ERROR:` message is printed and nobody enters

#### Scenario: Presence survives date change
- **WHEN** ana entered A01 and the system date then changes
- **THEN** ana is still inside A01

#### Scenario: Already inside
- **WHEN** a user holding the ticket is already inside the attraction
- **THEN** an `ERROR:` message is printed and nobody enters

#### Scenario: Type restrictions
- **WHEN** a group ticket is used at an INDIVIDUAL or SPECIAL attraction, or an individual ticket at a GROUP attraction
- **THEN** an `ERROR:` message is printed and nobody enters

#### Scenario: Individual SpeedPass entry
- **WHEN** T001 has unconsumed SpeedPasses and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly one of them is consumed and its identifier is printed

#### Scenario: Group SpeedPass entry is atomic
- **WHEN** one composed ticket of TG01 has no unconsumed SpeedPass and `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** an `ERROR:` message is printed, nobody enters and no SpeedPass is consumed

#### Scenario: Special Pass consumed automatically
- **WHEN** T001 has an unconsumed Special Pass and `ATTRACTION ENTER;A04;T001` is processed at a SPECIAL attraction
- **THEN** the user enters, the Special Pass becomes consumed and the output says so

#### Scenario: Special attraction rejections
- **WHEN** at a SPECIAL attraction the ticket has no Special Pass, its pass is consumed, or SPEED is requested
- **THEN** an `ERROR:` message is printed, nobody enters and no pass changes

### Requirement: Leaving an attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove that user from the attraction's occupants and free one place. Users who entered with a group ticket leave individually. It SHALL be rejected if the user is not inside.

#### Scenario: Group member leaves alone
- **WHEN** three users entered A01 with TG01 and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** ana is no longer inside and the other two remain

### Requirement: Listing occupants
`ATTRACTION LIST;AttractionIdentifier` SHALL print the email addresses of the users currently inside, or a controlled empty result, without changing the attraction.

#### Scenario: Empty attraction
- **WHEN** A01 has no occupants and `ATTRACTION LIST;A01` is processed
- **THEN** an empty-result message is printed without error

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL print the identifier, name, maximum capacity, number of users inside, number of available places and the users inside, from the attraction's own state and regardless of how users entered.

#### Scenario: Status after mixed entries
- **WHEN** A01 (capacity 20) has one user who entered individually and two who entered with a group ticket
- **THEN** status shows A01, its name, capacity 20, 3 inside, 17 available and the three users
