# attraction-management Specification

## Purpose

Defines the park's attractions: their identity, name and maximum capacity, the users currently inside each one, how users enter and leave them, and how their contents and status are consulted.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with a unique identifier, a name and a maximum capacity — the maximum number of people inside simultaneously, a positive integer — and no users inside.

#### Scenario: Create attractions
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` and `ATTRACTION CREATE;A02;Torre del Cielo;12` are processed
- **THEN** both are confirmed and attractions `A01` (capacity 20) and `A02` (capacity 12) exist and are empty

#### Scenario: Duplicate identifier
- **WHEN** `A01` exists and `ATTRACTION CREATE;A01;Otra;5` is processed
- **THEN** an error is printed and `A01` is unchanged

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0`, `ATTRACTION CREATE;A03;Noria;-2`, `ATTRACTION CREATE;A03;Noria;abc` or `ATTRACTION CREATE;A03;Noria` is processed
- **THEN** an error is printed and no attraction is created

### Requirement: Attraction occupants
An attraction SHALL hold references to the users currently inside it; users SHALL NOT hold any reference to the attraction they are in.

#### Scenario: Occupants kept by the attraction
- **WHEN** a user is inside attraction `A01`
- **THEN** the user is found by consulting `A01`'s occupants, and the user object has no attraction attribute

### Requirement: Attraction list
`ATTRACTION LIST;AttractionIdentifier` SHALL display the email addresses of the users inside the attraction at that moment, or a controlled empty result when there are none, without changing the attraction's state.

#### Scenario: Empty attraction
- **WHEN** `A01` has no users inside and `ATTRACTION LIST;A01` is processed
- **THEN** a message stating that there are no users inside `A01` is printed

#### Scenario: Users listed
- **WHEN** Ana and Pedro are inside `A01` and `ATTRACTION LIST;A01` is processed
- **THEN** `ana@correo.com` and `pedro@correo.com` are printed and both remain inside

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed and no such attraction exists
- **THEN** an error is printed

### Requirement: Entering with an individual ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL let the user of an existing individual ticket into the existing attraction when the ticket's date equals the current system date and the attraction has at least one available place. The date SHALL be checked only at entry; a user already inside SHALL stay inside when the system date changes.

#### Scenario: Valid entry
- **WHEN** the system date is `2027-04-15`, Ana has ticket `T001` dated `2027-04-15`, `A01` has capacity 20 and is empty, and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** a confirmation is printed and Ana is inside `A01` with 19 places available

#### Scenario: Ticket for another date
- **WHEN** the system date is `2027-04-14` and `ATTRACTION ENTER;A01;T001` is processed for ticket `T001` dated `2027-04-15`
- **THEN** an error is printed and nobody enters

#### Scenario: Unknown ticket or attraction
- **WHEN** `ATTRACTION ENTER;A01;T999` (no such ticket) or `ATTRACTION ENTER;A99;T001` (no such attraction) is processed
- **THEN** an error is printed and nobody enters

#### Scenario: Attraction full
- **WHEN** attraction `A03` has capacity 1 with Pedro inside and `ATTRACTION ENTER;A03;T001` is processed with Ana's valid ticket
- **THEN** an error about insufficient capacity is printed and Ana does not enter

#### Scenario: Presence independent of later date changes
- **WHEN** Ana entered `A01` on `2027-04-15` and then `SYSTEM DATE;2027-04-16` is processed
- **THEN** Ana is still inside `A01`

#### Scenario: Already inside
- **WHEN** Ana is inside `A01` and `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** an error is printed and Ana appears only once among `A01`'s users
