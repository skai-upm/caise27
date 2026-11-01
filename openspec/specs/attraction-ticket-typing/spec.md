# attraction-ticket-typing Specification

## Purpose

Specialises attractions by the kind of ticket they admit, expressing ticket access capabilities as interfaces and constraining each attraction type with bounded generics.

## Requirements

### Requirement: Attraction types
Attractions SHALL have one of the types `GENERAL`, `INDIVIDUAL`, `GROUP`, or `SPECIAL`. `GENERAL` SHALL admit individual and group tickets; `INDIVIDUAL` only individual tickets; `GROUP` only group tickets; `SPECIAL` only individual tickets, additionally applying the Special Pass rules.

#### Scenario: GROUP rejects an individual ticket
- **GIVEN** a `GROUP` attraction and a valid individual ticket
- **WHEN** the ticket is used to enter
- **THEN** an error message is reported and nobody enters

#### Scenario: INDIVIDUAL rejects a group ticket
- **GIVEN** an `INDIVIDUAL` attraction and a valid group ticket
- **WHEN** the ticket is used to enter
- **THEN** an error message is reported and nobody enters

#### Scenario: GENERAL admits both
- **GIVEN** a `GENERAL` attraction with room, a valid individual ticket, and a valid group ticket
- **WHEN** both are used to enter
- **THEN** all their users enter

### Requirement: Typed attraction creation
`ATTRACTION CREATE;<Id>;<Nombre>;<Capacidad>` SHALL keep creating a `GENERAL` attraction, and `ATTRACTION CREATE;<Id>;<Nombre>;<Capacidad>;<Tipo>` SHALL create an attraction of the given type; an unknown type SHALL be rejected.

#### Scenario: E2 syntax creates GENERAL
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** `A01` is a `GENERAL` attraction

#### Scenario: Explicit type
- **WHEN** `ATTRACTION CREATE;A02;Casa Magica;5;SPECIAL` is processed
- **THEN** `A02` is a `SPECIAL` attraction, and `ATTRACTION CREATE;A03;X;5;VIP` is rejected

### Requirement: Interfaces and bounded generics for access
Ticket access capabilities SHALL be modelled by interfaces implemented by the ticket classes, and attraction classes SHALL be parameterised by a type variable bounded by those interfaces that determines which tickets their entry operation accepts.

#### Scenario: Generic bound restricts admission
- **WHEN** the attraction classes are inspected
- **THEN** each declares a bounded type parameter tied to an access interface, and its entry operation only accepts tickets of that type
