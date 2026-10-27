# Spec Delta

## ADDED Requirements

### Requirement: Attraction types
`ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` SHALL create an attraction of type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`. The form without type SHALL create a `GENERAL` attraction. Any other type SHALL be rejected.

#### Scenario: Typed creation
- **WHEN** `ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL`, `ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP` and `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` are processed
- **THEN** the attractions are created with those types

#### Scenario: Default type
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** `A01` is a GENERAL attraction

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A05;Noria;10;VIP` is processed
- **THEN** an error is printed and no attraction is created

### Requirement: Ticket types accepted by attraction type
GENERAL attractions SHALL accept individual and group tickets; INDIVIDUAL attractions SHALL accept individual tickets only; GROUP attractions SHALL accept group tickets only; SPECIAL attractions SHALL accept individual tickets only and SHALL require an unconsumed Special Pass associated with the ticket. A ticket of a type not accepted SHALL be rejected without anyone entering.

#### Scenario: Group ticket at an individual attraction
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed for INDIVIDUAL attraction `A02`
- **THEN** an error is printed and nobody enters

#### Scenario: Individual ticket at a group attraction
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed for GROUP attraction `A03`
- **THEN** an error is printed and nobody enters

#### Scenario: Group ticket at a group attraction
- **WHEN** `ATTRACTION ENTER;A03;TG01` is processed with a valid group ticket
- **THEN** all members of `TG01` enter

#### Scenario: Special attraction without Special Pass
- **WHEN** `T001` has no unconsumed Special Pass and `ATTRACTION ENTER;A04;T001` is processed for SPECIAL attraction `A04`
- **THEN** an error is printed and nobody enters

#### Scenario: Special attraction with Special Pass
- **WHEN** `T001` has an unconsumed Special Pass and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** Ana enters `A04`

#### Scenario: General attraction accepts both
- **WHEN** valid tickets `T001` and `TG01` are used on GENERAL attraction `A01`
- **THEN** both entries succeed

### Requirement: Generic attraction design
Ticket access capabilities SHALL be represented by interfaces implemented by the ticket classes, and attractions SHALL be generic types bounded by those interfaces, so that each attraction's type parameter determines the tickets its typed operations accept. INDIVIDUAL and SPECIAL attractions SHALL both work with individual tickets, the Special Pass condition being part of the special attraction's own behaviour.

#### Scenario: Bounded generic parameters
- **WHEN** the attraction classes are inspected
- **THEN** the base attraction declares a type parameter bounded by the ticket access interface, and the general, individual, group and special attractions bind it to the general, individual, group and individual access interfaces respectively
