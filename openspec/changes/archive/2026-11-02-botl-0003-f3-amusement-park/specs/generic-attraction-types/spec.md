# Spec Delta

## Purpose

Defines attraction types that restrict which tickets may be used for entry, with the restriction expressed through ticket access-capability interfaces and bounded generic attractions.

## ADDED Requirements

### Requirement: Attraction types and creation syntax
`ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` SHALL create an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL; the Delivery 2 form without type, and stored attractions without type, SHALL be GENERAL. An unknown type SHALL be rejected. (F3-C07)

#### Scenario: Old syntax
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` runs
- **THEN** A01 is a GENERAL attraction

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A05;Otra;10;VIP` runs
- **THEN** an error is reported and no attraction is created

### Requirement: Accepted ticket kinds
GENERAL attractions SHALL accept individual and group tickets; INDIVIDUAL and SPECIAL only individual tickets (including group constituents used individually); GROUP only group tickets. A ticket of a non-accepted kind SHALL be rejected without changing occupancy. (F3-C07)

#### Scenario: Group ticket at INDIVIDUAL attraction
- **WHEN** a group ticket is used at an INDIVIDUAL or SPECIAL attraction
- **THEN** entry is rejected

#### Scenario: Individual ticket at GROUP attraction
- **WHEN** an individual ticket is used at a GROUP attraction
- **THEN** entry is rejected

### Requirement: Interfaces and bounded generics
Ticket access capabilities SHALL be represented by interfaces implemented by individual and group tickets, and attractions SHALL be generic types bounded by those interfaces whose type parameter determines the tickets they handle; INDIVIDUAL and SPECIAL SHALL share the individual-access capability, the Special Pass rule being part of the special attraction's behavior. (F3-C07)

#### Scenario: Design inspection
- **WHEN** the attraction classes are inspected
- **THEN** each type is an `Attraction<T>` with `T` bounded by an access interface, and its admission operation takes a `T`
