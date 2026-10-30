# attractions Specification

## Purpose

Defines park attractions, their capacity and occupants, and the commands to enter, leave, list and inspect them using tickets valid for the current date.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an empty attraction with a unique identifier, a name and a positive whole-number maximum capacity.

#### Scenario: Attraction created
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction A01 exists with capacity 20 and no occupants

#### Scenario: Invalid capacity or duplicate
- **WHEN** ATTRACTION CREATE uses capacity `0`, `-1` or `veinte`, or an existing identifier
- **THEN** an `ERROR:` message is printed and no attraction is created

### Requirement: Entering an attraction
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL let in every user holding the ticket (one user for an individual ticket, all composed users for a group ticket) when the attraction and ticket exist, the ticket's date equals the current system date, and there is room for all of them. Entry is all-or-nothing. The ticket date is checked only at entry.

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
