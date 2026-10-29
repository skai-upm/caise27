# Spec Delta

## ADDED Requirements

### Requirement: Attraction types
Every attraction SHALL have one of the types GENERAL, INDIVIDUAL, GROUP or SPECIAL. `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` SHALL create an attraction of the given type; `ATTRACTION CREATE;<identifier>;<name>;<capacity>` SHALL create a GENERAL attraction; an unknown type SHALL be rejected with an error message. `ATTRACTION STATUS` SHALL show the type.

#### Scenario: Create the four types
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL`, `ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL`, `ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP` and `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` are processed
- **THEN** the four attractions exist with those types

#### Scenario: Delivery 2 syntax creates GENERAL
- **WHEN** `ATTRACTION CREATE;A05;Noria;10` is processed
- **THEN** `A05` is a GENERAL attraction

#### Scenario: Unknown type rejected
- **WHEN** `ATTRACTION CREATE;A06;X;10;VIP` is processed
- **THEN** an error message is shown and `A06` is not created

### Requirement: Ticket types accepted by each attraction type
Normal entry SHALL be accepted only with ticket kinds allowed by the attraction type: GENERAL accepts individual and group tickets; INDIVIDUAL accepts only individual tickets; GROUP accepts only group tickets; SPECIAL accepts only individual tickets that have an unconsumed Special Pass. Other combinations SHALL be rejected with an error message and nobody SHALL enter.

#### Scenario: Group ticket at an individual attraction
- **WHEN** a valid group ticket is used to enter INDIVIDUAL attraction `A02`
- **THEN** an error message is shown and nobody enters

#### Scenario: Individual ticket at a group attraction
- **WHEN** a valid individual ticket is used to enter GROUP attraction `A03`
- **THEN** an error message is shown and nobody enters

#### Scenario: Special attraction requires a Special Pass
- **WHEN** an individual ticket without an unconsumed Special Pass is used at SPECIAL attraction `A04`, and then the same ticket after buying a Special Pass
- **THEN** the first entry is rejected and the second is accepted

#### Scenario: General attraction accepts both
- **WHEN** a valid individual ticket and a valid group ticket are used at GENERAL attraction `A01`
- **THEN** both entries are accepted

### Requirement: Access restrictions expressed with interfaces and generics
Ticket access capabilities SHALL be represented by interfaces implemented by individual and group tickets, and attractions SHALL be bounded generic types over those interfaces such that the generic parameter determines which tickets the attraction's entry operation accepts. Individual and special attractions SHALL use the same individual-ticket capability; the Special Pass condition SHALL be part of the special attraction's own behavior.

#### Scenario: Typed entry operation
- **WHEN** the attraction classes are inspected
- **THEN** each attraction type is parameterized with the access capability it accepts and its entry operation takes that capability type

### Requirement: Attraction type persisted
The attraction type SHALL be stored and reconstructed; stored attractions without a type SHALL be loaded as GENERAL.

#### Scenario: Restart keeps types
- **WHEN** attractions of the four types are saved and reloaded, together with a legacy line `A09;Vieja;5`
- **THEN** each keeps its type and `A09` is GENERAL
