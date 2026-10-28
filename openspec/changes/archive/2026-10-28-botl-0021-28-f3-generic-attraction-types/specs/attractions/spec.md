## MODIFIED Requirements

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

## ADDED Requirements

### Requirement: Attraction types restricted by generic ticket capabilities
Ticket access capabilities SHALL be represented by interfaces implemented by individual and group tickets, and each attraction type SHALL be a generic attraction bounded by the capability it accepts: GENERAL by general access, INDIVIDUAL and SPECIAL by individual access, GROUP by group access. The difference between INDIVIDUAL and SPECIAL SHALL lie in the special attraction's own entry rule, not in a different ticket hierarchy.

#### Scenario: Type restrictions encoded in the model
- **WHEN** the attraction classes are inspected
- **THEN** each one extends the generic attraction with a type argument that is one of the access capability interfaces, and individual and group tickets implement the corresponding interfaces
