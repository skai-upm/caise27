# attraction-catalog-and-occupancy Specification

## Purpose

Defines park attractions, with identity, name, capacity and the users currently inside, and the read-only occupant listing (F2-C05).

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with a unique identifier, a name, a positive integer maximum capacity, and an initially empty collection of users inside. Duplicate identifiers and non-positive or non-numeric capacities SHALL be rejected. The attraction SHALL reference its occupants; users SHALL NOT reference attractions.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction A01 named `Dragon Rojo` with capacity 20 and no occupants exists

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;abc` is processed
- **THEN** an error is reported and no attraction is created

#### Scenario: Duplicate identifier
- **WHEN** `ATTRACTION CREATE;A01;Otra;5` is processed and A01 exists
- **THEN** an error is reported and A01 is unchanged

### Requirement: Attraction occupant listing
`ATTRACTION LIST;AttractionIdentifier` SHALL display the emails of the users currently inside the attraction, or an empty-equivalent result when there are none, without changing any state. An unknown attraction SHALL be reported as an error.

#### Scenario: Empty attraction
- **WHEN** `ATTRACTION LIST;A01` is processed for an attraction with no occupants
- **THEN** a controlled empty result is shown

#### Scenario: Occupied attraction
- **WHEN** `ATTRACTION LIST;A01` is processed while Ana is inside A01
- **THEN** `ana@correo.com` is listed and the occupancy of A01 is unchanged
