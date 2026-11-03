# Spec Delta

## Purpose

Defines park attractions with identity, name, capacity and current occupants, and a read-only listing of who is inside.

## ADDED Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with a unique identifier, a name, a positive integer maximum capacity and an empty occupant collection.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is executed
- **THEN** attraction A01 exists with capacity 20 and no occupants

#### Scenario: Invalid attraction
- **WHEN** the identifier already exists or the capacity is not a positive integer
- **THEN** an error is reported and no attraction is created

### Requirement: Occupants are referenced by the attraction only
An attraction SHALL hold references to the users currently inside it; users SHALL NOT reference attractions.

#### Scenario: Occupant references
- **WHEN** a user enters an attraction
- **THEN** the attraction's occupants include that user and the user holds no attraction reference

### Requirement: Attraction occupant listing
`ATTRACTION LIST;AttractionIdentifier` SHALL display the email addresses of the users currently inside, an empty or equivalent result when there are none, and SHALL NOT change state.

#### Scenario: Empty attraction
- **WHEN** `ATTRACTION LIST;A01` runs on a newly created attraction
- **THEN** an empty or equivalent result is shown

#### Scenario: Occupied attraction
- **WHEN** `ATTRACTION LIST` runs on an attraction with occupants
- **THEN** their emails are displayed and occupancy is unchanged

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST` names a nonexistent attraction
- **THEN** an error is reported
