# Spec Delta

## Purpose

Defines the Amusement Park attraction types, which ticket types each accepts for entry, and the interface- and generics-based design that enforces those restrictions.

## ADDED Requirements

### Requirement: Attraction types
Every attraction SHALL have one type among `GENERAL`, `INDIVIDUAL`, `GROUP` and `SPECIAL`, chosen at creation (default `GENERAL`).

#### Scenario: Types created
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL`, `ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL`, `ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP` and `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` are processed
- **THEN** four attractions exist with those types

#### Scenario: Unknown type rejected
- **WHEN** `ATTRACTION CREATE;A05;X;10;VIP` is processed
- **THEN** an error reporting an invalid attraction type is printed and no attraction is created

### Requirement: Ticket types accepted per attraction type
`GENERAL` attractions SHALL accept individual and group tickets; `INDIVIDUAL` attractions only individual tickets; `GROUP` attractions only group tickets; `SPECIAL` attractions only individual tickets. `ATTRACTION ENTER` with a ticket the attraction does not accept MUST be rejected with an error and nobody enters.

#### Scenario: Group ticket at an individual attraction
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed with group ticket `TG01` valid today
- **THEN** an error reporting that `A02` does not accept `TG01` is printed and nobody enters

#### Scenario: Individual ticket at a group attraction
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed with individual ticket `T001` valid today
- **THEN** an error is printed and nobody enters

#### Scenario: General attraction accepts both
- **WHEN** `ATTRACTION ENTER;A01;T001` and `ATTRACTION ENTER;A01;TG01` are processed with valid tickets
- **THEN** the users of both tickets enter `A01`

### Requirement: Special attractions require a Special Pass
A `SPECIAL` attraction SHALL admit an individual ticket only if that ticket has an unconsumed Special Pass; otherwise entry is rejected with an error and nobody enters.

#### Scenario: Without Special Pass
- **GIVEN** individual ticket `T001` valid today without a Special Pass
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** an error reporting that an unconsumed Special Pass is required is printed

#### Scenario: With Special Pass
- **GIVEN** individual ticket `T002` valid today with unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T002` is processed
- **THEN** the ticket's user enters `A04`

### Requirement: Interfaces and bounded generics
Ticket access capabilities SHALL be represented by interfaces implemented by the ticket types (general admission by every ticket, individual access by individual tickets, group access by group tickets), and attractions SHALL be generic types bounded by those interfaces so the bound determines which tickets an attraction can admit and which ticket operations it uses. `INDIVIDUAL` and `SPECIAL` attractions MUST share the individual-access bound; the Special Pass condition belongs to the special attraction's own behavior.

#### Scenario: Bound restricts admission
- **WHEN** an individual-access attraction is asked whether it admits a group ticket
- **THEN** it answers that it does not, because the group ticket does not implement the individual-access capability
