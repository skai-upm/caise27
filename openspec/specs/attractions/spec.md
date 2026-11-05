# attractions Specification

## Purpose

Defines park attractions — identified, named elements with a maximum capacity that hold the users currently inside them — and how they are created and consulted.

## Requirements

### Requirement: ATTRACTION CREATE command
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with that unique identifier, name and maximum capacity (a positive integer: the maximum number of people inside simultaneously), initially with no occupants, and print `ATTRACTION CREATED`. A duplicate or empty identifier, an empty name, or a capacity that is not a positive integer MUST be rejected with an error message starting with `ERROR`.

#### Scenario: Attraction created
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** `ATTRACTION CREATED` is printed and attraction `A01` named `Dragon Rojo` with capacity 20 and no occupants exists

#### Scenario: Invalid data rejected
- **GIVEN** attraction `A01` exists
- **WHEN** `ATTRACTION CREATE;A01;Otra;10`, `ATTRACTION CREATE;A02;Torre del Cielo;0`, `ATTRACTION CREATE;A02;Torre del Cielo;-3` or `ATTRACTION CREATE;A02;Torre del Cielo;doce` is processed
- **THEN** an error message starting with `ERROR` is printed and no new attraction exists

### Requirement: Attraction occupants
Each attraction SHALL hold references to the users currently inside it, never more than its capacity; users SHALL NOT hold references to the attraction they are in.

#### Scenario: Unidirectional relationship
- **WHEN** the user and attraction types are inspected
- **THEN** the attraction exposes its occupants and the user has no attraction reference

### Requirement: ATTRACTION LIST command
`ATTRACTION LIST;AttractionIdentifier` SHALL display the email addresses of the users currently inside the attraction, one per line after a header line naming the attraction; when it has no users it SHALL display a controlled result stating that there are no users. The operation is read-only. An unknown identifier is rejected with an error message starting with `ERROR`.

#### Scenario: Empty attraction listed
- **GIVEN** attraction `A01` with no occupants
- **WHEN** `ATTRACTION LIST;A01` is processed
- **THEN** a result stating that `A01` has no users is printed and its occupants remain empty

#### Scenario: Occupants listed
- **GIVEN** Ana and Pedro are inside `A01`
- **WHEN** `ATTRACTION LIST;A01` is processed
- **THEN** `ana@correo.com` and `pedro@correo.com` are displayed and the occupants are unchanged

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed
- **THEN** an error message starting with `ERROR` is printed
