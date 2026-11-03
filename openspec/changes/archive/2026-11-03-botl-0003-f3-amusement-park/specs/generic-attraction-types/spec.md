# Spec Delta

## Purpose

Introduces GENERAL, INDIVIDUAL, GROUP and SPECIAL attractions whose accepted ticket kinds are enforced through ticket access interfaces and bounded generic attraction types (source F3-C07, sections 10 and 11).

## ADDED Requirements

### Requirement: Attraction types
`ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` SHALL create an attraction of type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`; any other type SHALL be rejected; the 3-parameter form and stored attractions without a type SHALL be GENERAL.

#### Scenario: Typed creation
- **WHEN** `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` is processed
- **THEN** a SPECIAL attraction `A04` with capacity 15 exists

#### Scenario: Old syntax creates GENERAL
- **WHEN** `ATTRACTION CREATE;A05;Noria;10` is processed
- **THEN** `A05` is a GENERAL attraction

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A06;Otra;10;VIP` is processed
- **THEN** an error is reported and no attraction is created

### Requirement: Accepted ticket kinds per type
GENERAL attractions SHALL accept individual and group tickets; INDIVIDUAL attractions only individual tickets; GROUP attractions only group tickets; SPECIAL attractions only individual tickets with an unconsumed Special Pass; other tickets SHALL be rejected without changing state.

#### Scenario: Group ticket at an INDIVIDUAL attraction
- **WHEN** a valid group ticket is used at an INDIVIDUAL attraction
- **THEN** entry is rejected and nobody enters

#### Scenario: Individual ticket at a GROUP attraction
- **WHEN** a valid individual ticket is used at a GROUP attraction
- **THEN** entry is rejected

### Requirement: Interfaces and bounded generics
Ticket access capabilities SHALL be represented by interfaces implemented by individual and group tickets, and attractions SHALL be generic classes bounded by those interfaces, with the type parameter determining which tickets an attraction can handle; INDIVIDUAL and SPECIAL SHALL share the individual access capability, the Special Pass rule belonging to the special attraction.

#### Scenario: Generic bound has an effect
- **WHEN** the attraction classes are inspected
- **THEN** each attraction type is parameterized by an access interface and its entry operation accepts only tickets implementing that interface
