# attractions Specification

## Purpose
Defines the park's attractions, each with a unique identifier, a name, a maximum capacity and the users currently inside it, and the commands to create attractions and list their occupants.

## Requirements

### Requirement: Create an attraction
The system SHALL create an attraction with `ATTRACTION CREATE;Identifier;Name;Capacity` when the identifier and name are non-empty, the identifier is not used by another attraction, and the capacity is a positive whole number. The new attraction SHALL have no occupants. Otherwise it SHALL reject the command with an error message and create nothing.

#### Scenario: Create attractions
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** a confirmation is displayed
- **AND** attraction `A01` named `Dragon Rojo` with capacity 20 and no occupants exists

#### Scenario: Invalid attraction is rejected
- **GIVEN** attraction `A01` exists
- **WHEN** `ATTRACTION CREATE;A01;Otra;5`, `ATTRACTION CREATE;A02;Torre;0`, `ATTRACTION CREATE;A02;Torre;-3`, `ATTRACTION CREATE;A02;Torre;doce`, `ATTRACTION CREATE;A02;;12` or `ATTRACTION CREATE;A02;Torre` is processed
- **THEN** an error message is displayed
- **AND** `A01` is unchanged and `A02` does not exist

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
The system SHALL include `ATTRACTION CREATE;<Identifier>;<Name>;<Capacity>` and `ATTRACTION LIST;<AttractionIdentifier>` in the output of `HELP`.

#### Scenario: HELP shows ATTRACTION commands
- **WHEN** `HELP` is processed
- **THEN** the output contains both ATTRACTION command syntaxes

### Requirement: Enter an attraction with an individual ticket
The system SHALL, for `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an individual ticket, add the ticket's user to the attraction's occupants when the attraction and ticket exist, the ticket date equals the current system date, the user is not already inside, and the attraction has at least one free place. A confirmation SHALL be displayed. Otherwise it SHALL reject the entry with an error message and leave the occupants unchanged.

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
The system SHALL include `ATTRACTION ENTER;<AttractionIdentifier>;<TicketIdentifier>` in the output of `HELP`.

#### Scenario: HELP shows ATTRACTION ENTER
- **WHEN** `HELP` is processed
- **THEN** the output contains `ATTRACTION ENTER;<AttractionIdentifier>;<TicketIdentifier>`
