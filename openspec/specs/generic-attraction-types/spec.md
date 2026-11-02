# generic-attraction-types Specification

## Purpose

Introduces attraction types that restrict which tickets may be used for entry, represented with ticket access interfaces and bounded generic attraction types.

## Requirements

### Requirement: Attraction types and accepted tickets
Attractions SHALL have one of the types GENERAL, INDIVIDUAL, GROUP or SPECIAL. GENERAL SHALL accept individual and group tickets; INDIVIDUAL SHALL accept individual tickets only; GROUP SHALL accept group tickets only; SPECIAL SHALL accept individual tickets only and require an unconsumed Special Pass for the ticket. A ticket of a type the attraction does not accept SHALL be rejected without changing state.

#### Scenario: Group ticket at an INDIVIDUAL attraction
- **WHEN** a group ticket is used at an INDIVIDUAL attraction
- **THEN** entry is rejected

#### Scenario: Individual ticket at a GROUP attraction
- **WHEN** an individual ticket is used at a GROUP attraction
- **THEN** entry is rejected

#### Scenario: Group ticket at a SPECIAL attraction
- **WHEN** a group ticket is used at a SPECIAL attraction
- **THEN** entry is rejected

### Requirement: Access interfaces and bounded generics
Ticket access capabilities SHALL be represented by interfaces implemented by the ticket types, and attractions SHALL be generic types bounded by those interfaces so that the type parameter determines the tickets an attraction can handle. INDIVIDUAL and SPECIAL attractions SHALL both work with individual tickets, the Special Pass rule being part of the special attraction's behavior.

#### Scenario: Code inspection
- **WHEN** the attraction and ticket classes are inspected
- **THEN** each attraction type fixes a bounded type parameter that its entry operation uses, and individual and group tickets implement the corresponding access interfaces
