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
