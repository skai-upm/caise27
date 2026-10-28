# attraction-types Specification

## Purpose
Defines the attraction types (GENERAL, INDIVIDUAL, GROUP, SPECIAL), which ticket types each accepts for entry, the Special Pass condition of special attractions, and the interface-and-generics design that enforces those restrictions.

## Requirements

### Requirement: Typed attraction creation
`ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` SHALL create an attraction of type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`; any other type SHALL be rejected with an `ERROR:` message. The form without type SHALL remain valid and create a `GENERAL` attraction.

#### Scenario: Create the four types
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL`, `ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL`, `ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP` and `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` are processed
- **THEN** each prints `ATTRACTION CREATED` and `ATTRACTION STATUS` shows its type

#### Scenario: Delivery 2 syntax
- **WHEN** `ATTRACTION CREATE;A05;Clasica;10` is processed
- **THEN** a GENERAL attraction is created

### Requirement: Accepted ticket types
GENERAL attractions SHALL accept individual and group tickets; INDIVIDUAL attractions SHALL accept only individual tickets; GROUP attractions SHALL accept only group tickets; SPECIAL attractions SHALL accept only individual tickets. Entry with a ticket type the attraction does not accept SHALL be rejected with an `ERROR:` message and nobody enters.

#### Scenario: Group ticket at an individual attraction
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed for INDIVIDUAL attraction `A02` and group ticket `TG01`
- **THEN** an `ERROR:` line is printed and nobody enters

#### Scenario: Individual ticket at a group attraction
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed for GROUP attraction `A03` and individual ticket `T001`
- **THEN** an `ERROR:` line is printed

### Requirement: Special Pass required for special attractions
Entry to a SPECIAL attraction SHALL require that the individual ticket has an unconsumed Special Pass; otherwise it SHALL be rejected with an `ERROR:` message. This condition SHALL be behavior of the special attraction itself, not a different ticket type.

#### Scenario: Without Special Pass
- **WHEN** `T001` has no unconsumed Special Pass and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** an `ERROR:` line about the Special Pass is printed

#### Scenario: With Special Pass
- **WHEN** `T001` (valid today) has an unconsumed Special Pass and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** the user of `T001` enters `A04`

### Requirement: Generic access design
Access capabilities SHALL be represented by interfaces implemented by the ticket classes, and attractions SHALL be generic types bounded by those interfaces, so that an attraction's entry operation only accepts tickets with the capability of its type parameter.

#### Scenario: Type parameter restricts entry
- **WHEN** the attraction classes are inspected
- **THEN** each attraction type extends `Attraction<T extends AccessTicket>` with the capability it accepts, and its entry operation takes a `T`
