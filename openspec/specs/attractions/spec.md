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

### Requirement: Attraction types
Every attraction SHALL have a type among `GENERAL`, `INDIVIDUAL`, `GROUP` and `SPECIAL`. `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` SHALL create an attraction of the given type; the syntax without type SHALL create a `GENERAL` attraction; an unknown type is rejected with an error message starting with `ERROR`. Stored attractions without a type SHALL be loaded as `GENERAL`, and `ATTRACTION STATUS` SHALL show the type.

#### Scenario: Typed creation
- **WHEN** `ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL`, `ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP` and `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` are processed
- **THEN** each prints `ATTRACTION CREATED` and `ATTRACTION STATUS` shows the corresponding type

#### Scenario: Delivery 2 syntax creates GENERAL
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** `A01` is a `GENERAL` attraction

#### Scenario: Unknown type and legacy data
- **WHEN** `ATTRACTION CREATE;A05;X;5;VIP` is processed
- **THEN** an error message starting with `ERROR` is printed
- **AND** an attractions file line `A01;Dragon Rojo;20` (no type) is loaded as a `GENERAL` attraction

### Requirement: Generic attractions bounded by access capabilities
Ticket access capabilities SHALL be represented by interfaces (general ticket access, individual access, group access) implemented by individual and group tickets, and attractions SHALL be generic types bounded by these interfaces such that an attraction can only admit tickets of its bound type; INDIVIDUAL and SPECIAL attractions both use individual access, the special conditions being the special attraction's own behavior.

#### Scenario: Type parameter restricts accepted tickets
- **WHEN** an attraction bounded by individual access is asked to admit a group ticket
- **THEN** it refuses it, and a GROUP attraction refuses an individual ticket, without changing occupants
