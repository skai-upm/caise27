## ADDED Requirements

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
