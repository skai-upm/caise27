# attractions Specification

## Purpose
Defines the park's attractions, each with identity, name, maximum capacity and the users currently inside, and how to consult those occupants.

## Requirements

### Requirement: ATTRACTION CREATE command
The system SHALL accept `ATTRACTION CREATE;Identifier;Name;Capacity;Type`, with `Type` one of `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`, and `ATTRACTION CREATE;Identifier;Name;Capacity`, which creates a `GENERAL` attraction, creating an attraction with that unique identifier, name, maximum capacity and type and no occupants. A duplicate identifier, an empty field, a capacity that is not a positive integer, an unknown type, or a wrong number of fields SHALL be rejected with an error message without creating an attraction.

#### Scenario: Create attractions
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` and `ATTRACTION CREATE;A02;Torre del Cielo;12` are processed
- **THEN** attractions `A01` (capacity 20) and `A02` (capacity 12) exist with no users inside

#### Scenario: Invalid attraction rejected
- **WHEN** `ATTRACTION CREATE;A01;Otra;5` (duplicate), `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A04;Noria;abc` is processed
- **THEN** an error message is displayed for each and no new attraction is created

#### Scenario: Typed creation
- **WHEN** `ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL`, `ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP` and `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` are processed
- **THEN** the attractions exist with types INDIVIDUAL, GROUP and SPECIAL respectively

#### Scenario: Default and unknown type
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` and `ATTRACTION CREATE;A05;Noria;5;VIP` are processed
- **THEN** `A01` is GENERAL and an error message is displayed for `A05`, which is not created

### Requirement: Attraction occupants
An attraction SHALL hold references to the users currently inside it, never more than its capacity and each user at most once; users SHALL NOT hold any reference to the attraction they are in.

#### Scenario: Capacity is never exceeded
- **WHEN** an attraction with capacity 1 already holds one user and another user is added
- **THEN** the addition is rejected and the attraction still holds one user

### Requirement: ATTRACTION LIST command
The system SHALL accept `ATTRACTION LIST;AttractionIdentifier` and display the e-mail addresses of the users currently inside that attraction, or a controlled empty result when there are none, without changing the attraction's state. An unknown attraction SHALL be reported with an error message.

#### Scenario: List occupants
- **WHEN** users `ana@correo.com` and `pedro@correo.com` are inside `A01` and `ATTRACTION LIST;A01` is processed
- **THEN** both e-mail addresses are displayed and the occupants of `A01` are unchanged

#### Scenario: Empty attraction
- **WHEN** no user is inside `A02` and `ATTRACTION LIST;A02` is processed
- **THEN** an empty-result message is displayed without error

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed and no such attraction exists
- **THEN** an error message is displayed

### Requirement: Attraction types restricted by generic ticket capabilities
Ticket access capabilities SHALL be represented by interfaces implemented by individual and group tickets, and each attraction type SHALL be a generic attraction bounded by the capability it accepts: GENERAL by general access, INDIVIDUAL and SPECIAL by individual access, GROUP by group access. The difference between INDIVIDUAL and SPECIAL SHALL lie in the special attraction's own entry rule, not in a different ticket hierarchy.

#### Scenario: Type restrictions encoded in the model
- **WHEN** the attraction classes are inspected
- **THEN** each one extends the generic attraction with a type argument that is one of the access capability interfaces, and individual and group tickets implement the corresponding interfaces
