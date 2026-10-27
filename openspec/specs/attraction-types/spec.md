# attraction-types Specification

## Purpose

Defines attraction types and the ticket types each accepts, represented with access-capability interfaces and attractions with bounded generic types.

## Requirements

### Requirement: Attraction creation with type
`ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` SHALL create an attraction of type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`; the form without type SHALL create a `GENERAL` attraction; any other type SHALL be rejected.

#### Scenario: Typed creation
- **WHEN** `ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL` is processed
- **THEN** the response is `ATTRACTION CREATED` and `A02` is an INDIVIDUAL attraction

#### Scenario: Delivery 2 syntax
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** `A01` is a GENERAL attraction

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A05;X;10;VIP` is processed
- **THEN** an error is reported and no attraction is created

### Requirement: Accepted ticket types
GENERAL attractions SHALL accept individual and group tickets; INDIVIDUAL attractions only individual tickets; GROUP attractions only group tickets; SPECIAL attractions only individual tickets. Entry with a ticket type the attraction does not accept SHALL be rejected without changing any state, and all Delivery 2 entry checks SHALL still apply.

#### Scenario: Group ticket at an INDIVIDUAL attraction
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed for INDIVIDUAL attraction `A02`
- **THEN** an error is reported and nobody enters

#### Scenario: Individual ticket at a GROUP attraction
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed for GROUP attraction `A03`
- **THEN** an error is reported and nobody enters

#### Scenario: Accepted combinations
- **WHEN** valid tickets enter A01 (GENERAL) with an individual and a group ticket, A02 (INDIVIDUAL) with an individual ticket and A03 (GROUP) with a group ticket
- **THEN** all entries succeed

#### Scenario: Group ticket at a SPECIAL attraction
- **WHEN** `ATTRACTION ENTER;A04;TG01` is processed for SPECIAL attraction `A04`
- **THEN** an error is reported

### Requirement: Access capabilities and generic attractions
Ticket access capabilities SHALL be represented by interfaces implemented by the ticket types, and attractions SHALL be generic types bounded by those interfaces whose entry operation only accepts the attraction's access type; INDIVIDUAL and SPECIAL attractions SHALL use the same individual-access capability, their difference being the special attraction's own behaviour.

#### Scenario: Typed entry operation
- **WHEN** the attraction classes are inspected
- **THEN** each concrete attraction binds the generic parameter to a capability interface and its admission operation takes that type as parameter

### Requirement: Type shown in status
`ATTRACTION STATUS` SHALL also show the attraction type.

#### Scenario: Status of a typed attraction
- **WHEN** `ATTRACTION STATUS;A03` is processed for GROUP attraction `A03`
- **THEN** the output contains `Type: GROUP`
