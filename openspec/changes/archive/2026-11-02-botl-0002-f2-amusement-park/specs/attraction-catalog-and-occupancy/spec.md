# Spec Delta

## Purpose

Lets the park define attractions with an identity, name and maximum capacity, each holding the users currently inside, and inspect those occupants without changing anything.

## ADDED Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with a unique identifier, a name, a positive integer maximum capacity and an empty occupant collection. (F2-C05)

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` runs
- **THEN** attraction A01 named `Dragon Rojo` with capacity 20 and no occupants exists

#### Scenario: Invalid attraction
- **WHEN** ATTRACTION CREATE uses an existing identifier, or a capacity that is zero, negative or not an integer
- **THEN** an error is reported and no attraction is created

### Requirement: Unidirectional occupancy
An attraction SHALL hold references to the users currently inside it; users SHALL NOT hold references to attractions. (F2-C05)

#### Scenario: Model inspection
- **WHEN** the user and attraction model is inspected
- **THEN** attractions reference users and users have no attraction reference

### Requirement: ATTRACTION LIST
`ATTRACTION LIST;AttractionIdentifier` SHALL display the email addresses of the users currently inside the attraction, or an explicit empty result when there are none, without changing state. (F2-C05)

#### Scenario: Empty attraction
- **WHEN** `ATTRACTION LIST;A01` runs on a new attraction
- **THEN** an empty result is reported in a controlled way

#### Scenario: Occupied attraction
- **WHEN** Ana and Pedro are inside A01 and `ATTRACTION LIST;A01` runs
- **THEN** their emails are displayed and occupancy is unchanged

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` runs and A99 does not exist
- **THEN** an error is reported
