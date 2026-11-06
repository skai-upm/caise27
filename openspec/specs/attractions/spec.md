# attractions Specification

## Purpose
Defines the park's attractions, each with a unique identifier, a name, a maximum capacity and the users currently inside it, and the commands to create attractions and list their occupants.

## Requirements

### Requirement: Create an attraction
The system SHALL create an attraction with `ATTRACTION CREATE;Identifier;Name;Capacity;Type`, where Type is `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`, or with `ATTRACTION CREATE;Identifier;Name;Capacity`, which creates a `GENERAL` attraction. It SHALL do so when the identifier and name are non-empty, the identifier is not used by another attraction, and the capacity is a positive whole number. The new attraction SHALL have no occupants. Otherwise, including for an unknown type, it SHALL reject the command with an error message and create nothing.

#### Scenario: Create attractions
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** a confirmation is displayed
- **AND** attraction `A01` named `Dragon Rojo` with capacity 20 and no occupants exists

#### Scenario: Invalid attraction is rejected
- **GIVEN** attraction `A01` exists
- **WHEN** `ATTRACTION CREATE;A01;Otra;5`, `ATTRACTION CREATE;A02;Torre;0`, `ATTRACTION CREATE;A02;Torre;-3`, `ATTRACTION CREATE;A02;Torre;doce`, `ATTRACTION CREATE;A02;;12` or `ATTRACTION CREATE;A02;Torre` is processed
- **THEN** an error message is displayed
- **AND** `A01` is unchanged and `A02` does not exist

#### Scenario: Typed creation
- **WHEN** `ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL`, `ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP` and `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` are processed
- **THEN** the three attractions exist with types INDIVIDUAL, GROUP and SPECIAL

#### Scenario: Default and unknown type
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** `A01` is a GENERAL attraction
- **AND** `ATTRACTION CREATE;A05;X;5;VIP` is rejected with an error message

### Requirement: Attraction occupants are referenced one way
An attraction SHALL hold the collection of users currently inside it, each at most once. Users SHALL NOT hold any reference to the attraction they are in.

#### Scenario: Occupants known only by the attraction
- **GIVEN** a user is inside attraction `A01`
- **WHEN** the occupants of `A01` are consulted
- **THEN** the user is included
- **AND** the user's own data does not refer to `A01`

### Requirement: List attraction occupants
The system SHALL display, for `ATTRACTION LIST;AttractionIdentifier`, the email address of each user currently inside the attraction. When it has no occupants, the result SHALL be a controlled empty result. The operation SHALL NOT change the attraction. An unknown identifier SHALL produce an error message.

#### Scenario: Empty attraction
- **GIVEN** attraction `A01` has no occupants
- **WHEN** `ATTRACTION LIST;A01` is processed
- **THEN** a message states that there are no users in `A01`
- **AND** `A01` still has no occupants and the same capacity

#### Scenario: Attraction with occupants
- **GIVEN** `ana@correo.com` and `pedro@correo.com` are inside `A01`
- **WHEN** `ATTRACTION LIST;A01` is processed
- **THEN** both email addresses are displayed
- **AND** the occupants are unchanged

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed and `A99` does not exist
- **THEN** an error message is displayed

### Requirement: ATTRACTION commands listed in HELP
The system SHALL include `ATTRACTION CREATE;<identifier>;<name>;<capacity>` and `ATTRACTION LIST;<attraction>` in the output of `HELP`.

#### Scenario: HELP shows ATTRACTION commands
- **WHEN** `HELP` is processed
- **THEN** the output contains both ATTRACTION command syntaxes

### Requirement: Enter an attraction with an individual ticket
The system SHALL, for `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an individual ticket at an attraction whose type accepts individual tickets, add the ticket's user to the attraction's occupants when the attraction and ticket exist, the ticket date equals the current system date, the user is not already inside, the attraction has at least one free place, and any additional condition of the attraction type is met. A confirmation SHALL be displayed. Otherwise it SHALL reject the entry with an error message and leave the occupants unchanged.

#### Scenario: Valid ticket and free capacity
- **GIVEN** system date 2027-04-15, attraction `A01` with capacity 20 and no occupants, and ticket `T001` for `ana@correo.com` dated 2027-04-15
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** a confirmation is displayed
- **AND** `ana@correo.com` is an occupant of `A01`

#### Scenario: Ticket not valid today
- **GIVEN** system date 2027-04-15 and ticket `T002` dated 2027-04-16
- **WHEN** `ATTRACTION ENTER;A01;T002` is processed
- **THEN** an error message is displayed and the occupants of `A01` are unchanged

#### Scenario: Attraction full
- **GIVEN** attraction `A03` with capacity 1 that already has one occupant, and a valid ticket for another user
- **WHEN** that user tries to enter `A03`
- **THEN** an error message is displayed and the occupants of `A03` are unchanged

#### Scenario: Unknown attraction or ticket, or user already inside
- **WHEN** `ATTRACTION ENTER` names an unknown attraction or ticket, or the ticket's user is already inside the attraction
- **THEN** an error message is displayed and no occupant is added

### Requirement: Presence does not depend on later date changes
The system SHALL check the ticket date only at the moment of entry. Users already inside an attraction SHALL remain inside when the system date changes afterwards.

#### Scenario: Date changes after entry
- **GIVEN** `ana@correo.com` entered `A01` with a ticket valid on 2027-04-15
- **WHEN** `SYSTEM DATE;2027-04-16` is processed
- **THEN** `ATTRACTION LIST;A01` still shows `ana@correo.com`

### Requirement: Single access mechanism for all ticket types
The system SHALL perform attraction entry through one operation that works on any ticket: it obtains from the ticket the users it admits, then applies the same validity and capacity checks to all of them at once, without separate access logic per ticket type.

#### Scenario: Individual ticket admits its user
- **GIVEN** a valid individual ticket for `ana@correo.com`
- **WHEN** it is used to enter an attraction
- **THEN** exactly `ana@correo.com` is admitted

### Requirement: ATTRACTION ENTER listed in HELP
The system SHALL include `ATTRACTION ENTER;<attraction>;<ticket>` in the output of `HELP`.

#### Scenario: HELP shows ATTRACTION ENTER
- **WHEN** `HELP` is processed
- **THEN** the output contains `ATTRACTION ENTER;<attraction>;<ticket>`

### Requirement: Enter an attraction with a group ticket
The system SHALL, for `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with a group ticket at an attraction whose type accepts group tickets, admit together every user associated with the individual tickets of its composition. It SHALL do so only when the group ticket's date equals the current system date, none of those users is already inside, and the attraction's free places are at least the number of those users. Entry SHALL be indivisible: if any condition fails, no user enters and an error message is displayed.

#### Scenario: Whole group enters
- **GIVEN** system date 2027-04-15, group ticket `TG01` dated 2027-04-15 composed for `ana@correo.com` and `pedro@correo.com`, and attraction `A01` with 20 free places
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** a confirmation listing both users is displayed
- **AND** both are occupants of `A01`

#### Scenario: Insufficient capacity for the whole group
- **GIVEN** attraction `A02` has 1 free place and group ticket `TG01` admits 2 users
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed
- **THEN** an error message is displayed
- **AND** no member of `TG01` is inside `A02`

#### Scenario: Group ticket for another day
- **GIVEN** group ticket `TG02` is dated after the current system date
- **WHEN** `ATTRACTION ENTER;A01;TG02` is processed
- **THEN** an error message is displayed and no user enters

### Requirement: Leave an attraction
The system SHALL, for `ATTRACTION EXIT;AttractionIdentifier;Email`, remove that user from the attraction's occupants and release their place when the attraction exists and the user is inside it. Otherwise it SHALL display an error message and leave the occupants unchanged. Users who entered with a group ticket SHALL each leave individually with their own email.

#### Scenario: User leaves and frees a place
- **GIVEN** `ana@correo.com` is inside `A01`
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** a confirmation is displayed
- **AND** `ana@correo.com` is no longer inside `A01` and one more place is available

#### Scenario: Group members leave one by one
- **GIVEN** `ana@correo.com` and `pedro@correo.com` entered `A01` with group ticket `TG01`
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** only `pedro@correo.com` remains inside `A01`

#### Scenario: User not inside or unknown attraction
- **WHEN** `ATTRACTION EXIT` names a user who is not inside the attraction, or an unknown attraction
- **THEN** an error message is displayed and no occupant changes

### Requirement: Attraction status
The system SHALL, for `ATTRACTION STATUS;AttractionIdentifier`, display the attraction identifier, name, maximum capacity, number of users currently inside, number of available places and the users currently inside. The values SHALL be taken from the attraction's own state and SHALL NOT depend on whether occupants entered with individual or group tickets. The operation SHALL NOT change the attraction. An unknown identifier SHALL produce an error message.

#### Scenario: Status after entries and an exit
- **GIVEN** attraction `A01` (Dragon Rojo, capacity 20) that `ana@correo.com` entered with an individual ticket and `pedro@correo.com` and `luis@correo.com` entered with a group ticket, after which `luis@correo.com` left
- **WHEN** `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Dragon Rojo`, capacity 20, 2 users inside, 18 available places, and the emails `ana@correo.com` and `pedro@correo.com`

#### Scenario: Status of an empty attraction
- **GIVEN** attraction `A02` with capacity 12 and no occupants
- **WHEN** `ATTRACTION STATUS;A02` is processed
- **THEN** the output shows 0 users inside and 12 available places

### Requirement: ATTRACTION EXIT and STATUS listed in HELP
The system SHALL include `ATTRACTION EXIT;<attraction>;<email>` and `ATTRACTION STATUS;<attractionIdentifier>` in the output of `HELP`.

#### Scenario: HELP shows ATTRACTION EXIT and STATUS
- **WHEN** `HELP` is processed
- **THEN** the output contains both command syntaxes

### Requirement: Attraction types restrict accepted tickets
GENERAL attractions SHALL accept individual and group tickets. INDIVIDUAL and SPECIAL attractions SHALL accept individual tickets only. GROUP attractions SHALL accept group tickets only. Entry with a ticket kind that the attraction's type does not accept SHALL be rejected with an error message and no occupant change.

#### Scenario: Group ticket at an individual attraction
- **GIVEN** INDIVIDUAL attraction `A02` and a valid group ticket `TG01`
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed
- **THEN** an error message is displayed and nobody enters `A02`

#### Scenario: Individual ticket at a group attraction
- **GIVEN** GROUP attraction `A03` and a valid individual ticket `T001`
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed
- **THEN** an error message is displayed and nobody enters `A03`

#### Scenario: Accepted kinds enter normally
- **WHEN** a valid individual ticket enters INDIVIDUAL attraction `A02` and a valid group ticket enters GROUP attraction `A03`
- **THEN** both entries succeed

### Requirement: Special attractions require a Special Pass
A SPECIAL attraction SHALL admit an individual ticket only if that ticket has an unconsumed Special Pass. The pass SHALL be used automatically, without any extra option in the entry command.

#### Scenario: No Special Pass
- **GIVEN** SPECIAL attraction `A04` and a valid ticket `T001` without a Special Pass
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** an error message is displayed and nobody enters

#### Scenario: With Special Pass
- **GIVEN** ticket `T001` has unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** the user enters `A04`

### Requirement: Access capabilities restrict attractions by type parameter
Ticket access capabilities SHALL be represented by interfaces that individual and group tickets implement. Each attraction type SHALL be a generic attraction bounded by the capability it accepts, and that bound SHALL determine which tickets its entry operation can handle. INDIVIDUAL and SPECIAL attractions SHALL use the same individual-ticket capability. The difference between them SHALL be the special attraction's own entry rule.

#### Scenario: Type parameter restricts handling
- **WHEN** a GROUP attraction is asked whether it can handle an individual ticket
- **THEN** it reports that it cannot, because the ticket does not implement the group-access capability

### Requirement: Attraction type in status and HELP
`ATTRACTION STATUS` SHALL show the attraction's type, and `HELP` SHALL include `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`.

#### Scenario: Status shows type
- **GIVEN** SPECIAL attraction `A04`
- **WHEN** `ATTRACTION STATUS;A04` is processed
- **THEN** the output contains `TYPE: SPECIAL`
