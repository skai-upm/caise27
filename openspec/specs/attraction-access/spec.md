# attraction-access Specification

## Purpose
Defines how users enter park attractions with a ticket valid for the current system date, respecting attraction capacity.

## Requirements

### Requirement: ATTRACTION ENTER with an individual ticket
The system SHALL accept `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` and, when the attraction and an individual ticket with that identifier exist and the ticket's date equals the current system date, add the ticket's user to the attraction's occupants.

#### Scenario: User enters with a ticket for today
- **WHEN** the system date is 2027-04-15, ticket `T001` for `ana@correo.com` is dated 2027-04-15, attraction `A01` has free capacity, and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** `ana@correo.com` is listed by `ATTRACTION LIST;A01`

### Requirement: Ticket validity checked at entry
Entry SHALL be rejected with an error message when the ticket does not exist or its date differs from the current system date. The date SHALL be checked only on entry: later changes of the system date SHALL NOT remove users already inside.

#### Scenario: Ticket for another date
- **WHEN** the system date is 2027-04-14, ticket `T001` is dated 2027-04-15 and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** an error message is displayed and nobody enters `A01`

#### Scenario: Unknown ticket or attraction
- **WHEN** `ATTRACTION ENTER;A01;NOPE` or `ATTRACTION ENTER;A99;T001` is processed
- **THEN** an error message is displayed and no attraction changes

#### Scenario: Presence survives date change
- **WHEN** `ana@correo.com` entered `A01` with a ticket for today and then `SYSTEM DATE` moves to the next day
- **THEN** `ana@correo.com` is still listed in `A01`

### Requirement: Capacity on entry
Entry SHALL be rejected with an error message when the attraction lacks capacity for every user the ticket admits; in that case nobody enters. A user already inside the attraction SHALL NOT enter it again.

#### Scenario: Attraction full
- **WHEN** attraction `A01` has capacity 1 and already holds one user, and another user's valid ticket is used with `ATTRACTION ENTER;A01;T002`
- **THEN** an error message is displayed and `A01` still holds exactly one user

#### Scenario: Already inside
- **WHEN** `ana@correo.com` is inside `A01` and `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** an error message is displayed and she appears once in `A01`

### Requirement: Common access mechanism
Attraction access SHALL be implemented through the common ticket abstraction, admitting the users of the ticket's individual tickets, with no separate access mechanism per ticket type.

#### Scenario: Access through the abstraction
- **WHEN** the access operation receives any ticket
- **THEN** the users admitted are those referenced by the individual tickets the ticket consists of

### Requirement: ATTRACTION ENTER with a group ticket
When the ticket used with `ATTRACTION ENTER` is a group ticket valid for the current system date, all users of the individual tickets in its composition SHALL enter the attraction together. Access SHALL be indivisible: if the attraction lacks capacity for all of them, or any of them is already inside, none SHALL enter and an error message SHALL be displayed.

#### Scenario: Whole group enters
- **WHEN** group ticket `TG01` for today has 3 members and attraction `A01` has 3 free places, and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** all 3 members are inside `A01`

#### Scenario: Insufficient capacity for the group
- **WHEN** group ticket `TG01` for today has 3 members and attraction `A01` has 2 free places, and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** an error message is displayed and no member enters `A01`

#### Scenario: Group ticket for another date
- **WHEN** group ticket `TG01` is dated tomorrow and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** an error message is displayed and nobody enters

### Requirement: ATTRACTION EXIT command
The system SHALL accept `ATTRACTION EXIT;AttractionIdentifier;Email` and remove the user with that e-mail from the attraction's occupants, releasing one place. Users who entered with a group ticket SHALL leave individually. An unknown attraction or a user not inside it SHALL be rejected with an error message.

#### Scenario: User leaves
- **WHEN** `ana@correo.com` is inside `A01` and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** `ana@correo.com` is no longer inside `A01` and one more place is available

#### Scenario: Group member leaves alone
- **WHEN** three users entered `A01` with a group ticket and one of them exits with her e-mail
- **THEN** the other two remain inside `A01`

#### Scenario: User not inside
- **WHEN** `pedro@correo.com` is not inside `A01` and `ATTRACTION EXIT;A01;pedro@correo.com` is processed
- **THEN** an error message is displayed and the occupants of `A01` are unchanged

### Requirement: ATTRACTION STATUS command
The system SHALL accept `ATTRACTION STATUS;AttractionIdentifier` and display, from the attraction's own state, its identifier, name, maximum capacity, number of users inside, number of available places and the users inside, regardless of whether they entered with individual or group tickets. An unknown attraction SHALL be reported with an error message.

#### Scenario: Status after entries and exits
- **WHEN** attraction `A01` (Dragon Rojo, capacity 20) holds 3 users who entered with a group ticket and 1 with an individual ticket, and one of them exits, and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Dragon Rojo`, capacity 20, 3 users inside, 17 available places and the e-mails of the 3 users inside

### Requirement: Entry restricted by attraction type
`ATTRACTION ENTER` SHALL admit individual and group tickets in GENERAL attractions, only individual tickets in INDIVIDUAL and SPECIAL attractions, and only group tickets in GROUP attractions; a ticket of a type the attraction does not accept SHALL be rejected with an error message and nobody enters.

#### Scenario: Group ticket at an individual attraction
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed for INDIVIDUAL attraction `A02` and a valid group ticket `TG01`
- **THEN** an error message is displayed and nobody enters `A02`

#### Scenario: Individual ticket at a group attraction
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed for GROUP attraction `A03` and a valid individual ticket `T001`
- **THEN** an error message is displayed and nobody enters `A03`

#### Scenario: Accepted ticket types enter
- **WHEN** valid tickets are used at attractions of a type that accepts them (individual at GENERAL and INDIVIDUAL, group at GENERAL and GROUP)
- **THEN** their users enter

### Requirement: Special attraction requires a Special Pass
A SPECIAL attraction SHALL admit an individual ticket only when an unconsumed Special Pass is associated with that ticket; otherwise entry SHALL be rejected with an error message.

#### Scenario: Without Special Pass
- **WHEN** valid individual ticket `T001` has no unconsumed Special Pass and `ATTRACTION ENTER;A04;T001` is processed for SPECIAL attraction `A04`
- **THEN** an error message is displayed and nobody enters

#### Scenario: With Special Pass
- **WHEN** valid individual ticket `T001` has an unconsumed Special Pass and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** the ticket's user enters `A04`
