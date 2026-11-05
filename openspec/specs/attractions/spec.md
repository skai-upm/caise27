# attractions Specification

## Purpose
Defines park attractions: their identity, name, maximum capacity and current occupants, how they are created, and how their occupants are consulted.

## Requirements

### Requirement: Attraction creation
The command `ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with that unique identifier, a non-empty name and a maximum capacity given as a positive whole number. The attraction SHALL start with no occupants. A duplicate identifier, empty field, or non-positive or non-numeric capacity SHALL be rejected with an error, and no attraction SHALL be created or changed.

#### Scenario: Create attractions
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** a confirmation is shown, and attraction `A01` named `Dragon Rojo` exists with capacity 20 and no occupants

#### Scenario: Duplicate identifier rejected
- **GIVEN** attraction `A01` exists named `Dragon Rojo`
- **WHEN** `ATTRACTION CREATE;A01;Otra;5` is processed
- **THEN** an error is shown and `A01` keeps its name and capacity

#### Scenario: Invalid capacity rejected
- **WHEN** `ATTRACTION CREATE;A03;Noria;0`, `ATTRACTION CREATE;A03;Noria;-2`, `ATTRACTION CREATE;A03;Noria;diez` or `ATTRACTION CREATE;A03;Noria` is processed
- **THEN** an error is shown and no attraction `A03` exists

### Requirement: Occupants of an attraction
Each attraction SHALL hold references to the users currently inside it, never more than its capacity. Users SHALL NOT hold references to the attraction they are in.

#### Scenario: New attraction is empty
- **GIVEN** attraction `A02` was just created with capacity 12
- **WHEN** its occupants are inspected
- **THEN** the occupant collection is empty

### Requirement: Listing attraction occupants
The command `ATTRACTION LIST;AttractionIdentifier` SHALL display the e-mail addresses of the users currently inside the attraction. If it has no occupants, it SHALL produce a controlled empty result. The operation SHALL NOT change the attraction's state. An unknown identifier SHALL be reported as an error.

#### Scenario: List occupants
- **GIVEN** `ana@correo.com` and `pedro@correo.com` are inside `A01`
- **WHEN** `ATTRACTION LIST;A01` is processed
- **THEN** both e-mail addresses are displayed and the occupants are unchanged

#### Scenario: Empty attraction
- **GIVEN** attraction `A01` has no occupants
- **WHEN** `ATTRACTION LIST;A01` is processed
- **THEN** an empty result is displayed in a controlled way and `A01` still has no occupants

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed and `A99` does not exist
- **THEN** an error is shown

### Requirement: Attraction entry with an individual ticket
The command `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL admit the user associated with an existing individual ticket into an existing attraction. The ticket's date SHALL equal the current system date and the attraction SHALL have free capacity. Otherwise the entry SHALL be rejected with an error and occupants SHALL remain unchanged.

#### Scenario: Valid ticket and free capacity
- **GIVEN** the system date 2027-04-15, ticket `T001` for `ana@correo.com` dated 2027-04-15, and attraction `A01` with free capacity
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** a confirmation is shown and `ana@correo.com` is inside `A01`

#### Scenario: Ticket not valid today
- **GIVEN** ticket `T002` dated 2027-04-16 and the system date 2027-04-15
- **WHEN** `ATTRACTION ENTER;A01;T002` is processed
- **THEN** an error is shown and the occupants of `A01` are unchanged

#### Scenario: Attraction full
- **GIVEN** attraction `A02` whose occupants already equal its capacity
- **WHEN** a valid ticket is used to enter `A02`
- **THEN** an error is shown and no one enters

#### Scenario: Unknown ticket or attraction
- **WHEN** `ATTRACTION ENTER` names a ticket or attraction that does not exist
- **THEN** an error is shown and no occupants change

#### Scenario: User already inside
- **GIVEN** `ana@correo.com` is already inside `A01`
- **WHEN** another valid ticket of `ana@correo.com` is used to enter `A01`
- **THEN** an error is shown and `ana@correo.com` appears only once

### Requirement: Entry date checked only at entry
The ticket's date SHALL be checked only when entering. A user's presence in an attraction SHALL NOT depend on later changes to the system date.

#### Scenario: Date changes after entry
- **GIVEN** `ana@correo.com` entered `A01` with a ticket valid on 2027-04-15
- **WHEN** `SYSTEM DATE;2027-04-20` is processed
- **THEN** `ana@correo.com` is still inside `A01`

### Requirement: Attraction entry with a group ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with a group ticket valid on the current system date SHALL admit together all the users of the tickets that make up the group ticket, using the same access operation as individual tickets. Group access SHALL be indivisible: if free capacity cannot hold all of them, or any of them is already inside, none SHALL enter.

#### Scenario: Whole group fits
- **GIVEN** group ticket `TG01` dated today for 3 members, and attraction `A01` with 3 or more free places
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** all 3 members are inside `A01`

#### Scenario: Not enough capacity for the group
- **GIVEN** group ticket `TG01` dated today for 3 members, and attraction `A02` with 2 free places
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed
- **THEN** an error is shown and no member enters `A02`

#### Scenario: Group ticket not valid today
- **GIVEN** group ticket `TG02` dated after the current system date
- **WHEN** `ATTRACTION ENTER;A01;TG02` is processed
- **THEN** an error is shown and no one enters

### Requirement: Leaving an attraction
The command `ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove that user from the attraction's occupants and free their place. Users who entered with a group ticket SHALL leave individually with their own e-mail. An unknown attraction or a user who is not inside SHALL be reported as an error, leaving occupants unchanged.

#### Scenario: User leaves
- **GIVEN** `ana@correo.com` is inside `A01`
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** `ana@correo.com` is no longer inside and one more place is available

#### Scenario: Group members leave one by one
- **GIVEN** the members of `TG01` entered `A01` together
- **WHEN** one member processes `ATTRACTION EXIT` with their e-mail
- **THEN** only that member leaves and the others remain inside

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT;A01;luis@correo.com` is processed and that user is not inside `A01`
- **THEN** an error is shown and occupants are unchanged

### Requirement: Attraction status
The command `ATTRACTION STATUS;AttractionIdentifier` SHALL display the attraction identifier, name, maximum capacity, number of users currently inside, number of available places and the users currently inside. These SHALL be obtained from the attraction's own state, treating every occupant the same whatever ticket they entered with. Unknown attractions SHALL be reported as an error.

#### Scenario: Status after mixed entries
- **GIVEN** attraction `A01` "Dragon Rojo" with capacity 20, which `ana@correo.com` entered with an individual ticket and two members entered with a group ticket
- **WHEN** `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Dragon Rojo`, capacity 20, 3 inside, 17 available and the three e-mails

#### Scenario: Status reflects exits
- **GIVEN** the previous state
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` and then `ATTRACTION STATUS;A01` are processed
- **THEN** the output shows 2 inside and 18 available

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION STATUS;A99` is processed
- **THEN** an error is shown
