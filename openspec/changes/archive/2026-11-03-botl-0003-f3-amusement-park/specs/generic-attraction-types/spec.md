# Spec Delta

## Purpose

Introduces attraction types that restrict which tickets may be used for entry, modelled with access interfaces and bounded generics.

## ADDED Requirements

### Requirement: Attraction types
`ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` SHALL create an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL; the Delivery-2 form without a type, and stored attractions without a type, SHALL be GENERAL.

#### Scenario: Typed creation
- **WHEN** `ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL` is executed
- **THEN** A02 is an INDIVIDUAL attraction

#### Scenario: Legacy syntax
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is executed
- **THEN** A01 is a GENERAL attraction

#### Scenario: Unknown type
- **WHEN** the type is not one of the four values
- **THEN** an error is reported and no attraction is created

### Requirement: Accepted ticket kinds
GENERAL attractions SHALL accept individual and group tickets, INDIVIDUAL and SPECIAL only individual tickets, GROUP only group tickets; any other ticket kind SHALL be rejected without changing state.

#### Scenario: Group ticket at individual attraction
- **WHEN** a group ticket is used at an INDIVIDUAL attraction
- **THEN** entry is rejected

#### Scenario: Individual ticket at group attraction
- **WHEN** an individual ticket is used at a GROUP attraction
- **THEN** entry is rejected

### Requirement: Interfaces and bounded generics
Ticket access capabilities SHALL be represented by interfaces implemented by the ticket types, and attractions SHALL use bounded generic types over those interfaces that restrict the tickets their entry operation handles; INDIVIDUAL and SPECIAL SHALL share the individual-ticket capability.

#### Scenario: Code inspection
- **WHEN** the attraction classes are inspected
- **THEN** each type is parameterised by an access interface that bounds the ticket accepted by its entry operation

### Requirement: Type shown in status
`ATTRACTION STATUS` SHALL also display the attraction type.

#### Scenario: Status type
- **WHEN** `ATTRACTION STATUS;A04` runs on a SPECIAL attraction
- **THEN** the output includes the type SPECIAL
