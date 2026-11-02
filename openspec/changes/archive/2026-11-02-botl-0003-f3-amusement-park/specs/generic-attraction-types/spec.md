# Spec Delta

## Purpose

Defines the GENERAL, INDIVIDUAL, GROUP and SPECIAL attraction types and their enforcement through ticket access interfaces and bounded generics (F3-C07).

## ADDED Requirements

### Requirement: Attraction types
Attractions SHALL have a type: GENERAL accepts individual and group tickets; INDIVIDUAL accepts individual tickets only; GROUP accepts group tickets only; SPECIAL accepts individual tickets only and additionally requires an unconsumed Special Pass. A ticket of a kind the attraction does not accept SHALL be rejected without changing state.

#### Scenario: Group ticket at an INDIVIDUAL attraction
- **WHEN** a valid group ticket is used to enter an INDIVIDUAL attraction
- **THEN** entry is rejected and nobody enters

#### Scenario: Individual ticket at a GROUP attraction
- **WHEN** a valid individual ticket is used to enter a GROUP attraction
- **THEN** entry is rejected

#### Scenario: Group ticket at a SPECIAL attraction
- **WHEN** a group ticket is used to enter a SPECIAL attraction
- **THEN** entry is rejected

### Requirement: Access interfaces and bounded generics
Ticket access capabilities SHALL be represented by interfaces implemented by individual and group tickets, and each attraction type SHALL be a generic attraction bounded by the access interface it accepts, so that the generic parameter determines which tickets it can handle. INDIVIDUAL and SPECIAL attractions SHALL share the individual-access capability; the Special Pass condition SHALL belong to the special attraction's behaviour.

#### Scenario: Code inspection
- **WHEN** the attraction and ticket classes are inspected
- **THEN** attractions declare a type parameter bounded by an access interface and use it to decide which tickets they accept
