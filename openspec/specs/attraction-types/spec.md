# attraction-types Specification

## Purpose

Restricts which kinds of tickets each attraction admits according to its type.

## Requirements

### Requirement: Attraction types and admitted tickets
The system SHALL support the attraction types `GENERAL`, `INDIVIDUAL`, `GROUP` and `SPECIAL`. `GENERAL` SHALL admit individual and group tickets, `INDIVIDUAL` only individual tickets, `GROUP` only group tickets and `SPECIAL` only individual tickets. An entry with a ticket kind the attraction does not admit SHALL be rejected without admitting anybody.

#### Scenario: GROUP rejects an individual ticket
- **GIVEN** `ATTRACTION CREATE;A02;Coches;10;GROUP` and Ana's ticket `T001` valid today
- **WHEN** `ATTRACTION ENTER;A02;T001` is processed
- **THEN** an error is reported and nobody enters

#### Scenario: INDIVIDUAL rejects a group ticket
- **GIVEN** an `INDIVIDUAL` attraction `A03` and group ticket `TG01` valid today
- **WHEN** `ATTRACTION ENTER;A03;TG01` is processed
- **THEN** an error is reported and nobody enters

#### Scenario: GENERAL admits both
- **GIVEN** a `GENERAL` attraction `A01`
- **WHEN** `T001` and `TG01` are used to enter
- **THEN** both entries succeed

#### Scenario: SPECIAL works with individual tickets
- **GIVEN** a `SPECIAL` attraction `A04` and `T001` with an unused Special Pass
- **WHEN** `ATTRACTION ENTER;A04;T001` and `ATTRACTION ENTER;A04;TG01` are processed
- **THEN** the individual ticket is admitted (consuming its Special Pass) and the group ticket is rejected

### Requirement: Access capabilities in the type system
Ticket access capabilities SHALL be represented by interfaces implemented by the ticket classes, and each attraction type SHALL be parameterised by a bounded generic type that restricts, at compile time, which ticket capability its typed entry operation accepts; tickets obtained at run time from commands SHALL be checked against that bound before entry.

#### Scenario: Compile-time restriction
- **WHEN** code calls the typed entry operation of an `INDIVIDUAL` attraction with a group ticket
- **THEN** it does not compile, while the same call with an individual ticket does

#### Scenario: Runtime check of command input
- **WHEN** a ticket identifier from a command resolves to a ticket of an unsupported kind
- **THEN** the attraction rejects it before any state change
